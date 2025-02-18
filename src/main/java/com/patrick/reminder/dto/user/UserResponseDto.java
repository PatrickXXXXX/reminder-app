package com.patrick.reminder.dto.user;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Long id;
    private String sub;
    private String username;
    private String email;
    private String telegramId;
}
