package unit.com.patrick.reminder.service;

import com.patrick.reminder.entity.Reminder;
import com.patrick.reminder.entity.User;
import com.patrick.reminder.repository.ReminderRepository;
import com.patrick.reminder.service.notification.NotificationService;
import com.patrick.reminder.telegram.TelegramBotClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TelegramBotClient telegramBotClient;

    @InjectMocks
    private NotificationService notificationService;

    private Reminder dueReminder; // reminder, который "пора"
    private Reminder futureReminder; // reminder, который ещё не пора

    @BeforeEach
    void setup() {
        // Пользователь
        User user = new User();
        user.setEmail("user@example.com");
        user.setTelegramId("12345");

        dueReminder = new Reminder();
        dueReminder.setId(1L);
        dueReminder.setName("Due Reminder");
        dueReminder.setDescription("Desc");
        dueReminder.setRemindDate(LocalDate.now()); // сегодня
        dueReminder.setRemindTime(LocalTime.now().minusMinutes(1)); // уже пора
        dueReminder.setSent(false);
        dueReminder.setUser(user);

        futureReminder = new Reminder();
        futureReminder.setId(2L);
        futureReminder.setName("Future Reminder");
        futureReminder.setDescription("Desc2");
        futureReminder.setRemindDate(LocalDate.now().plusDays(1)); // завтра
        futureReminder.setRemindTime(LocalTime.now());
        futureReminder.setSent(false);
        futureReminder.setUser(user);
    }

    @Test
    void checkAndSendReminders_SendsOnlyDue() {
        // given
        when(reminderRepository.findAll()).thenReturn(Arrays.asList(dueReminder, futureReminder));

        // when
        notificationService.checkAndSendReminders();

        // then
        // Ожидаем, что mailSender + telegramBotClient вызваны для dueReminder
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        verify(telegramBotClient, times(1)).sendMessage(eq("12345"), contains("Due Reminder"));

        // futureReminder "завтра" -> не должно отправляться
        verify(telegramBotClient, never()).sendMessage(eq("12345"), contains("Future Reminder"));

        // сохранение isSent(true) только для dueReminder
        verify(reminderRepository).save(argThat(r -> r.getId() == 1L && r.isSent()));
        verify(reminderRepository, never()).save(argThat(r -> r.getId() == 2L));
    }

    @Test
    void checkAndSendReminders_AlreadySent_NoResend() {
        // given
        dueReminder.setSent(true); // уже отправлено
        when(reminderRepository.findAll()).thenReturn(List.of(dueReminder));

        // when
        notificationService.checkAndSendReminders();

        // then
        // никакой отправки почты/телеграм не будет
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(telegramBotClient, never()).sendMessage(anyString(), anyString());
        // и reminderRepository.save() не вызывается, т. к. уже sent
        verify(reminderRepository, never()).save(any(Reminder.class));
    }
}
