package com.patrick.reminder.quartz;

import com.patrick.reminder.service.notification.NotificationService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * Класс Quartz Job, который вызывается по расписанию (Trigger).
 * Внутри обращается к NotificationService, чтобы отправить уведомления.
 */
@Component
public class ReminderJob implements Job {

    private final NotificationService notificationService;

    /**
     * Через конструктор внедряем наш сервис, занимающийся отправкой уведомлений.
     */
    public ReminderJob(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Метод execute(JobExecutionContext) вызывается Quartz-движком по расписанию.
     * Здесь просто передаём управление методу checkAndSendReminders().
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        notificationService.checkAndSendReminders();
    }
}
