package com.patrick.reminder.controller;

import com.patrick.reminder.dto.reminder.ReminderFilterRequestDto;
import com.patrick.reminder.dto.reminder.ReminderResponseDto;
import com.patrick.reminder.dto.reminder.ReminderSearchRequestDto;
import com.patrick.reminder.service.reminder.ReminderQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для поиска, сортировки и фильтрации (Reminder).
 */
@RestController
@RequestMapping("/api/v1/reminder")
public class ReminderQueryController {

    private final ReminderQueryService queryService;

    public ReminderQueryController(ReminderQueryService queryService) {
        this.queryService = queryService;
    }

    /**
     * Сортировка напоминаний.
     */
    @GetMapping("/sort")
    public ResponseEntity<List<ReminderResponseDto>> sortReminders(
            @RequestParam String by,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String sub = jwt.getSubject();
        List<ReminderResponseDto> sortedReminders = queryService.sortReminders(sub, by);
        return ResponseEntity.ok(sortedReminders);
    }

    /**
     * Фильтрация напоминаний по диапазонам дат/времени.
     */
    @GetMapping("/filter")
    public ResponseEntity<List<ReminderResponseDto>> filterReminders(
            ReminderFilterRequestDto filterDto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String sub = jwt.getSubject();
        List<ReminderResponseDto> filteredReminders = queryService.filterReminders(sub, filterDto);
        return ResponseEntity.ok(filteredReminders);
    }

    /**
     * Поиск напоминаний по конкретным полям (названию, дате, времени).
     */
    @GetMapping("/search")
    public ResponseEntity<List<ReminderResponseDto>> searchReminders(
            ReminderSearchRequestDto searchDto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String sub = jwt.getSubject();
        List<ReminderResponseDto> foundReminders = queryService.searchReminders(sub, searchDto);
        return ResponseEntity.ok(foundReminders);
    }
}
