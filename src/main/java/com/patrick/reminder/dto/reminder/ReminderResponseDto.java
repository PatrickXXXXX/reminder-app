package com.patrick.reminder.dto.reminder;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO для возврата информации о напоминании.
 */
@Data
public class ReminderResponseDto {
    private Long id;
    private String name;
    private String description;

    private LocalDate remindDate;
    private LocalTime remindTime;

    private Long userId;
    private String userSub;
}
