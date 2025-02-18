package com.patrick.reminder.controller;


import com.patrick.reminder.dto.user.UserRequestDto;
import com.patrick.reminder.dto.user.UserResponseDto;
import com.patrick.reminder.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Создаём (или обновляем) пользователя на основе информации из токена.
     * В теле запроса (JSON) приходят только дополнительные поля
     * (например, username, email, telegramId),
     * а уникальный идентификатор (sub) берём из JWT.
     */
    @PostMapping("/create")
    public ResponseEntity<UserResponseDto> createUser(
            @RequestBody UserRequestDto dto,
            @AuthenticationPrincipal Jwt jwt // Берём токен из SecurityContext
    ) {
        // Извлекаем sub (уникальный ID пользователя) из токена
        String sub = jwt.getSubject();
        // Делаем create/update пользователя с этим sub
        UserResponseDto createdUser = userService.createOrUpdateUser(sub, dto);
        return ResponseEntity.ok(createdUser);
    }

    /**
     * Получаем пользователя только если его sub совпадает с sub
     * в токене (чтобы не давать доступ к чужим данным).
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserResponseDto user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Сравниваем sub из БД (или UserResponseDto) с sub из токена
        // Предположим, что UserResponseDto тоже хранит sub, или
        // userService.getUserById(id) даёт возможность узнать sub пользователя.
        String tokenSub = jwt.getSubject();
        if (!user.getSub().equals(tokenSub)) {
            // Если не совпадает — возвращаем 403 Forbidden
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(user);
    }

    /**
     * Удаляем пользователя только если sub совпадает.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserResponseDto user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Проверяем sub
        String tokenSub = jwt.getSubject();
        if (!user.getSub().equals(tokenSub)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.deleteUserById(id);
        return ResponseEntity.ok().build();
    }
}
