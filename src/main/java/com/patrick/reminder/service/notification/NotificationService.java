package com.patrick.reminder.service.notification;

import com.patrick.reminder.entity.Reminder;
import com.patrick.reminder.repository.ReminderRepository;
import com.patrick.reminder.telegram.TelegramBotClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class NotificationService {

    private final ReminderRepository reminderRepository;
    private final JavaMailSender mailSender;
    private final TelegramBotClient telegramBotClient;

    @Autowired
    public NotificationService(ReminderRepository reminderRepository,
                               JavaMailSender mailSender,
                               TelegramBotClient telegramBotClient) {
        this.reminderRepository = reminderRepository;
        this.mailSender = mailSender;
        this.telegramBotClient = telegramBotClient;
    }

    public void checkAndSendReminders() {
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        // Получаем все напоминания
        List<Reminder> allReminders = reminderRepository.findAll();

        // Проверяем для каждого, настало ли время
        for (Reminder reminder : allReminders) {
            if (!reminder.isSent() && isReminderDue(reminder, today, nowTime)) {
                sendEmail(reminder);
                sendTelegram(reminder);

                reminder.setSent(true);
                reminderRepository.save(reminder);
            }
        }
    }

    /**
     * Проверяем, не пришло ли время (или прошло) для напоминания.
     */
    private boolean isReminderDue(Reminder r, LocalDate today, LocalTime nowTime) {
        // Если r.getRemindDate() < today => уже прошло
        if (r.getRemindDate().isBefore(today)) {
            return true;
        }
        // Если r.getRemindDate() == today => сравниваем время
        if (r.getRemindDate().isEqual(today)) {
            // Если r.getRemindTime() <= nowTime => пора
            return !r.getRemindTime().isAfter(nowTime);
        }
        // Иначе напоминание в будущем
        return false;
    }

    private void sendEmail(Reminder reminder) {
        String toEmail = reminder.getUser().getEmail();
        String subject = "Напоминание: " + reminder.getName();

        String text = "Подробности: " + reminder.getDescription()
                + "\nДата: " + reminder.getRemindDate()
                + "\nВремя: " + reminder.getRemindTime();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);

        mailSender.send(mailMessage);
        System.out.println("Почтовое уведомление отправлено на " + toEmail + " | Subject: " + subject);
    }

    private void sendTelegram(Reminder reminder) {
        String telegramId = reminder.getUser().getTelegramId();

        String message = "Напоминание: " + reminder.getName()
                + "\n" + reminder.getDescription()
                + "\nДата: " + reminder.getRemindDate()
                + "\nВремя: " + reminder.getRemindTime();

        telegramBotClient.sendMessage(telegramId, message);
    }
}
