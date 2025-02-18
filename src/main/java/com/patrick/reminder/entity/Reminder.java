package com.patrick.reminder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "reminders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 4096)
    private String description;

    private LocalDate remindDate;
    private LocalTime remindTime;

    @Column(name = "sent", nullable = false)
    private boolean sent = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
