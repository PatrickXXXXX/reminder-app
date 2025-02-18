package com.patrick.reminder.dto.reminder;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReminderFilterRequestDto {

    // Напоминания до указанной даты
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate beforeDate;

    // Напоминания после указанной даты
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate afterDate;

    // Напоминания до указанного времени
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime beforeTime;

    // Напоминания после указанного времени
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime afterTime;
}
