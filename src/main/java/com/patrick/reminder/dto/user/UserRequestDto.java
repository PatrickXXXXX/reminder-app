package com.patrick.reminder.dto.user;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {
    private String username;
    private String email;
    private String telegramId;
}
