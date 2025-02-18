package com.patrick.reminder.service.reminder;

import com.patrick.reminder.dto.reminder.ReminderFilterRequestDto;
import com.patrick.reminder.dto.reminder.ReminderResponseDto;
import com.patrick.reminder.dto.reminder.ReminderSearchRequestDto;
import com.patrick.reminder.entity.Reminder;
import com.patrick.reminder.entity.User;
import com.patrick.reminder.mapper.ReminderMapper;
import com.patrick.reminder.repository.ReminderRepository;
import com.patrick.reminder.repository.UserRepository;
import com.patrick.reminder.specification.ReminderSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class ReminderQueryService {

    private final ReminderRepository reminderRepository;
    private final ReminderMapper reminderMapper;
    private final UserRepository userRepository; // Чтобы найти id пользователя по sub

    @Autowired
    public ReminderQueryService(ReminderRepository reminderRepository,
                                ReminderMapper reminderMapper,
                                UserRepository userRepository) {
        this.reminderRepository = reminderRepository;
        this.reminderMapper = reminderMapper;
        this.userRepository = userRepository;
    }

    // метод сортировки
    public List<ReminderResponseDto> sortReminders(String userSub, String by) {
        Specification<Reminder> spec = ReminderSpecification.sortBy(by);
        List<Reminder> reminders = reminderRepository.findAll(spec);
        return reminders.stream()
                .map(reminderMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    // метод для фильтрации
    public List<ReminderResponseDto> filterReminders(String userSub, ReminderFilterRequestDto filterDto) {
        Long userId = userRepository.findBySub(userSub)
                .orElseThrow(() -> new RuntimeException("User not found with sub = " + userSub))
                .getId();

        // Начинаем с базовой спецификации "hasUser"
        Specification<Reminder> spec = ReminderSpecification.hasUser(userId);

        // Добавляем фильтрацию по дате
        if (filterDto.getBeforeDate() != null) {
            spec = spec.and(ReminderSpecification.filterByBeforeDate(filterDto.getBeforeDate()));
        }
        if (filterDto.getAfterDate() != null) {
            spec = spec.and(ReminderSpecification.filterByAfterDate(filterDto.getAfterDate()));
        }

        // Добавляем фильтрацию по времени
        if (filterDto.getBeforeTime() != null) {
            spec = spec.and(ReminderSpecification.filterByBeforeTime(filterDto.getBeforeTime()));
        }
        if (filterDto.getAfterTime() != null) {
            spec = spec.and(ReminderSpecification.filterByAfterTime(filterDto.getAfterTime()));
        }

        // Получаем список напоминаний
        List<Reminder> reminders = reminderRepository.findAll(spec);

        // Преобразуем в DTO
        return reminders.stream()
                .map(reminderMapper::toResponseDto)
                .collect(Collectors.toList());
    }


    // Метод для поиска напоминаний
    public List<ReminderResponseDto> searchReminders(String userSub, ReminderSearchRequestDto searchDto) {
        Long userId = userRepository.findBySub(userSub)
                .orElseThrow(() -> new RuntimeException("User not found with sub = " + userSub))
                .getId();

        // Базовая спецификация - только напоминания текущего пользователя
        Specification<Reminder> spec = ReminderSpecification.hasUser(userId);

        // Частичный поиск по названию
        if (searchDto.getName() != null && !searchDto.getName().isEmpty()) {
            spec = spec.and(ReminderSpecification.hasNameLike(searchDto.getName()));
        }

        // Частичный поиск по описанию
        if (searchDto.getDescription() != null && !searchDto.getDescription().isEmpty()) {
            spec = spec.and(ReminderSpecification.hasDescriptionLike(searchDto.getDescription()));
        }

        // Поиск по точной дате
        if (searchDto.getRemindDate() != null) {
            spec = spec.and(ReminderSpecification.hasRemindDate(searchDto.getRemindDate()));
        }

        // Поиск по точному времени
        if (searchDto.getRemindTime() != null) {
            spec = spec.and(ReminderSpecification.hasRemindTime(searchDto.getRemindTime()));
        }

        // Выполняем запрос
        List<Reminder> reminders = reminderRepository.findAll(spec);

        // Преобразуем результат в DTO
        return reminders.stream()
                .map(reminderMapper::toResponseDto)
                .collect(Collectors.toList());
    }

}

