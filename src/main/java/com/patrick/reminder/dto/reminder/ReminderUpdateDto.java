package com.patrick.reminder.dto.reminder;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO для обновления напоминания.
 */
@Data
public class ReminderUpdateDto {
    private Long id;

    private String name;
    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate remindDate;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime remindTime;
}
