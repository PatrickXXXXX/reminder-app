package com.patrick.reminder.quartz;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурационный класс Quartz:
 *  - Создаёт JobDetail и связывает его с ReminderJob
 *  - Создаёт Trigger (расписание) для запуска этой job
 */
@Configuration
public class QuartzConfig {

    /**
     * Описывает саму Job (какой класс хотим выполнять).
     * .storeDurably() означает, что JobDetail сохраняется,
     * даже если нет Trigger, который к нему привязан.
     */
    @Bean
    public JobDetail reminderJobDetail() {
        return JobBuilder.newJob(ReminderJob.class)
                .withIdentity("reminderJob") // имя (ID) job (необязательно)
                .storeDurably()              // job будет сохранена
                .build();
    }

    /**
     * Описывает расписание (Trigger),
     * указывая, когда и как часто запускать reminderJob.
     *
     * Пример cron-выражения "0 * * * * ?" = каждую минуту на 0 секунде:
     *   - секунды = 0
     *   - минуты = каждые
     *   - часы = любые
     *   - день месяца = любой
     *   - месяц = любой
     *   - день недели = любой
     */
    @Bean
    public Trigger reminderJobTrigger(JobDetail reminderJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(reminderJobDetail)          // к какой job привязываемся
                .withIdentity("reminderTrigger")    // имя (ID) триггера (необязательно)
                .withSchedule(CronScheduleBuilder.cronSchedule("0 * * * * ?"))
                .build();
    }
}
