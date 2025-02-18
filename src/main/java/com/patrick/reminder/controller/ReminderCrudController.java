package com.patrick.reminder.controller;

import com.patrick.reminder.dto.reminder.ReminderCreatetDto;
import com.patrick.reminder.dto.reminder.ReminderResponseDto;
import com.patrick.reminder.dto.reminder.ReminderUpdateDto;
import com.patrick.reminder.service.reminder.ReminderCrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для CRUD-операций и пагинации (Reminder).
 */
@RestController
@RequestMapping("/api/v1/reminder")
public class ReminderCrudController {

    private final ReminderCrudService crudService;

    public ReminderCrudController(ReminderCrudService crudService) {
        this.crudService = crudService;
    }

    // ------------------ CREATE ------------------

    @PostMapping("/create")
    public ResponseEntity<ReminderResponseDto> createReminder(
            @RequestBody ReminderCreatetDto dto,
            @AuthenticationPrincipal Jwt jwt // Эта аннотация извлекает информацию о текущем пользователе из токена JWT

    ) {

        // sub — это строка, получаемая из объекта Jwt (JSON Web Token),
        // которая представляет собой субъект (subject) токена,
        // используется для идентификации пользователя, для которого был выдан токен.
        String sub = jwt.getSubject();
        ReminderResponseDto created = crudService.createReminder(sub, dto);

        // ResponseEntity — это контейнер для HTTP-ответа в Spring,
        // который включает статусный код, заголовки и тело ответа.
        // Здесь тело ответа будет содержать объект типа ReminderResponseDto,
        // а сам ответ будет иметь статус 200 OK, если он выполнится успешно
        return ResponseEntity.ok(created);
    }

    // ------------------ READ ------------------

    @GetMapping("/{id}")
    public ResponseEntity<ReminderResponseDto> getReminder(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String sub = jwt.getSubject();

        // Достаточно передать в сервис только id
        ReminderResponseDto reminder = crudService.getReminder(id);

        if (reminder == null) {
            return ResponseEntity.notFound().build();
        }

        // Если напоминание найдено, но оно принадлежит другому пользователю (проверка по sub
        // — идентификатору пользователя), возвращается статус 403 (FORBIDDEN).
        if (!reminder.getUserSub().equals(sub)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(reminder);
    }

    @GetMapping("/list")
    public ResponseEntity<List<ReminderResponseDto>> getAllReminders(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String sub = jwt.getSubject();
        List<ReminderResponseDto> reminders = crudService.getAllReminders(sub);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<ReminderResponseDto>> getRemindersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String sub = jwt.getSubject();
        PageRequest pr = PageRequest.of(page, size); // создается объект, который управляет пагинацией
        Page<ReminderResponseDto> pageResult = crudService.getRemindersPaged(sub, pr);

        // Пагинированный результат возвращается как объект типа Page,
        // который включает в себя не только данные (напоминания), но и дополнительную
        // информацию о пагинации, такую как общее количество страниц, текущая страница и т.д.
        return ResponseEntity.ok(pageResult);
    }

    // ------------------ UPDATE ------------------

    @PutMapping("/update")
    public ResponseEntity<ReminderResponseDto> updateReminder(
            @RequestBody ReminderUpdateDto dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String sub = jwt.getSubject();
        ReminderResponseDto existing = crudService.getReminder(dto.getId());
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        if (!existing.getUserSub().equals(sub)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ReminderResponseDto updated = crudService.updateReminder(dto);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }


    // ------------------ DELETE ------------------

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteReminder(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String sub = jwt.getSubject();
        ReminderResponseDto reminder = crudService.getReminder(id);
        if (reminder == null) {
            return ResponseEntity.notFound().build();
        }

        if (!reminder.getUserSub().equals(sub)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean ok = crudService.deleteReminder(id);
        if (!ok) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}
