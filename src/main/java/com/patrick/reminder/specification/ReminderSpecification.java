package com.patrick.reminder.specification;

import com.patrick.reminder.entity.Reminder;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReminderSpecification {

    /**
     * Сортировка по переданному полю (name, remindDate, remindTime).
     */
    public static Specification<Reminder> sortBy(String by) {
        return (root, query, criteriaBuilder) -> {
            // Устанавливаем сортировку в запрос
            switch (by) {
                case "name":
                    query.orderBy(criteriaBuilder.asc(root.get("name")));
                    break;
                case "remindDate":
                    query.orderBy(criteriaBuilder.asc(root.get("remindDate")));
                    break;
                case "remindTime":
                    query.orderBy(criteriaBuilder.asc(root.get("remindTime")));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid sort parameter: " + by);
            }
            // Возвращаем null, так как сам фильтр не накладывается
            return null;
        };
    }

    /**
     * Фильтрация по пользователю (напоминания принадлежат конкретному userId).
     */
    public static Specification<Reminder> hasUser(Long userId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    /**
     * Фильтрация по дате: напоминания ДО указанной даты (beforeDate).
     */
    public static Specification<Reminder> filterByBeforeDate(LocalDate date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("remindDate"), date);
    }

    /**
     * Фильтрация по дате: напоминания ПОСЛЕ указанной даты (afterDate).
     */
    public static Specification<Reminder> filterByAfterDate(LocalDate date) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("remindDate"), date);
    }

    /**
     * Фильтрация по времени: напоминания ДО указанного времени (beforeTime).
     */
    public static Specification<Reminder> filterByBeforeTime(LocalTime time) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("remindTime"), time);
    }

    /**
     * Фильтрация по времени: напоминания ПОСЛЕ указанного времени (afterTime).
     */
    public static Specification<Reminder> filterByAfterTime(LocalTime time) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("remindTime"), time);
    }

    /**
     * Частичный поиск по названию (LIKE %...%).
     * Игнорируем регистр, приводя и поле, и искомую строку к нижнему регистру.
     */
    public static Specification<Reminder> hasNameLike(String name) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    /**
     * Поиск по точному совпадению даты (remindDate).
     */
    public static Specification<Reminder> hasRemindDate(LocalDate date) {
        return (root, query, cb) ->
                cb.equal(root.get("remindDate"), date);
    }

    /**
     * Поиск по точному совпадению времени (remindTime).
     */
    public static Specification<Reminder> hasRemindTime(LocalTime time) {
        return (root, query, cb) ->
                cb.equal(root.get("remindTime"), time);
    }

    // Частичный поиск по описанию
    public static Specification<Reminder> hasDescriptionLike(String description) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    }
}
