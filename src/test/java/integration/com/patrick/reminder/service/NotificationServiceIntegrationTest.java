package integration.com.patrick.reminder.service;

import com.patrick.reminder.ReminderApplication;
import com.patrick.reminder.entity.Reminder;
import com.patrick.reminder.entity.User;
import com.patrick.reminder.repository.ReminderRepository;
import com.patrick.reminder.repository.UserRepository;
import com.patrick.reminder.service.notification.NotificationService;
import com.patrick.reminder.telegram.TelegramBotClient;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * NotificationServiceIntegrationTest:
 * Тестирует интеграционно логику отправки уведомлений.
 *  - Поднимает реальный Spring-контекст, Liquibase, базу
 *  - Напрямую использует ReminderRepository, UserRepository
 *  - Подменяет реальный JavaMailSender и TelegramBotClient на моки,
 *    чтобы проверить факты вызовов (а не отправлять реальную почту/телеграм).
 */
@SpringBootTest(classes = ReminderApplication.class)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NotificationServiceIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private JavaMailSender mailSender;

    @MockBean
    private TelegramBotClient telegramBotClient;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> mailCaptor;

    /**
     * Создаёт пользователя в базе — у него есть email и telegramId.
     * Возвращает сохранённого пользователя.
     */
    private User createUser(String sub, String email, String telegramId) {
        User user = User.builder()
                .sub(sub)
                .username("TestUser_" + sub)
                .email(email)
                .telegramId(telegramId)
                .build();
        return userRepository.save(user);
    }

    @Test
    @Order(1)
    @DisplayName("checkAndSendReminders: отправляет уведомления только для 'просроченных' (remindDate+remindTime <= now), обновляет sent=true")
    void testCheckAndSendReminders() {
        // Создаем двух пользователей
        User user1 = createUser("sub1", "user1@example.com", "user1_telegram");
        User user2 = createUser("sub2", "user2@example.com", "user2_telegram");

        // reminder1: дата+время уже прошли (на 5 минут раньше текущего)
        Reminder reminder1 = Reminder.builder()
                .name("Name1")
                .description("Desc1")
                .remindDate(LocalDate.now())
                .remindTime(LocalTime.now().minusMinutes(5))
                .user(user1)
                .build();

        // reminder2: прошли на 1 минуту
        Reminder reminder2 = Reminder.builder()
                .name("Name2")
                .description("Desc2")
                .remindDate(LocalDate.now())
                .remindTime(LocalTime.now().minusMinutes(1))
                .user(user2)
                .build();

        // reminder3: remind ещё не наступил (завтра)
        Reminder reminder3 = Reminder.builder()
                .name("Name3")
                .description("Desc3")
                .remindDate(LocalDate.now().plusDays(1))
                .remindTime(LocalTime.now()) // любое время, главное что дата +1 день
                .user(user1)
                .build();

        // reminder4: remind через 10 минут
        Reminder reminder4 = Reminder.builder()
                .name("Name4")
                .description("Desc4")
                .remindDate(LocalDate.now())
                .remindTime(LocalTime.now().plusMinutes(10))
                .user(user2)
                .build();

        reminderRepository.save(reminder1);
        reminderRepository.save(reminder2);
        reminderRepository.save(reminder3);
        reminderRepository.save(reminder4);

        // Запускаем логику отправки
        notificationService.checkAndSendReminders();

        // Проверяем: reminder1 и reminder2 должны были быть отправлены (mail + telegram), reminder3 и reminder4 — нет
        verify(mailSender, times(2)).send(mailCaptor.capture());
        verify(telegramBotClient, times(2)).sendMessage(anyString(), anyString());

        // Проверяем, что письма ушли конкретным адресатам
        List<SimpleMailMessage> allMails = mailCaptor.getAllValues();
        assertThat(allMails).hasSize(2);
        assertThat(allMails.get(0).getTo()).containsAnyOf("user1@example.com", "user2@example.com");
        assertThat(allMails.get(1).getTo()).containsAnyOf("user1@example.com", "user2@example.com");

        // Смотрим в базе, что reminder1 и reminder2 теперь sent=true
        Reminder r1InDb = reminderRepository.findById(reminder1.getId()).orElseThrow();
        Reminder r2InDb = reminderRepository.findById(reminder2.getId()).orElseThrow();
        Reminder r3InDb = reminderRepository.findById(reminder3.getId()).orElseThrow();
        Reminder r4InDb = reminderRepository.findById(reminder4.getId()).orElseThrow();

        assertThat(r1InDb.isSent()).isTrue();
        assertThat(r2InDb.isSent()).isTrue();
        assertThat(r3InDb.isSent()).isFalse();
        assertThat(r4InDb.isSent()).isFalse();

        // Повторный вызов checkAndSendReminders() не отправит снова уже "sent" напоминания
        reset(mailSender, telegramBotClient);
        notificationService.checkAndSendReminders();
        verifyNoInteractions(mailSender);
        verifyNoInteractions(telegramBotClient);
    }
}
