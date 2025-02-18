package com.patrick.reminder.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String sub;        // Уникальный идентификатор от OAuth2-провайдера

    private String username;   // Имя пользователя
    private String email;      // Почта
    private String telegramId; // ID в Telegram
}
