package com.patrick.reminder.repository;

import com.patrick.reminder.entity.Reminder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long>, JpaSpecificationExecutor<Reminder> {

    // Для списка напоминаний конкретного пользователя
    List<Reminder> findByUserId(Long userId);

    // Для пагинации
    Page<Reminder> findByUserId(Long userId, Pageable pageable);

    // Тут могут быть другие методы, если нужно
}
