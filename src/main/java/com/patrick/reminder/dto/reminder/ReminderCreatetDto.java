package com.patrick.reminder.dto.reminder;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO для создания напоминания.
 * Принимаем два отдельных поля: remindDate и remindTime.
 */
@Data
public class ReminderCreatetDto {

    private String name;
    private String description;

    // Разделённые поля
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate remindDate;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime remindTime;
}
