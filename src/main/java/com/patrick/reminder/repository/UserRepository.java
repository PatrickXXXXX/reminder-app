package com.patrick.reminder.repository;

import com.patrick.reminder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Интерфейс Spring Data JPA для работы с сущностью User.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // Метод для поиска пользователя по sub
    Optional<User> findBySub(String sub);
}
