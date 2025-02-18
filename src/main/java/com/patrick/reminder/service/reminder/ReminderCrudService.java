package com.patrick.reminder.service.reminder;

import com.patrick.reminder.dto.reminder.ReminderCreatetDto;
import com.patrick.reminder.dto.reminder.ReminderResponseDto;
import com.patrick.reminder.dto.reminder.ReminderUpdateDto;
import com.patrick.reminder.entity.Reminder;
import com.patrick.reminder.entity.User;
import com.patrick.reminder.mapper.ReminderMapper;
import com.patrick.reminder.repository.ReminderRepository;
import com.patrick.reminder.repository.UserRepository;
import com.patrick.reminder.specification.ReminderSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис, отвечающий за базовые CRUD-операции и пагинацию для Reminder.
 * <p>
 * Предполагаем, что:
 * - Сущность Reminder хранит поля (name, description, remindDate, remindTime, user, ...)
 * - Создание/обновление происходит через MapStruct.
 */
@Service
public class ReminderCrudService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final ReminderMapper reminderMapper;

    public ReminderCrudService(ReminderRepository reminderRepository,
                               UserRepository userRepository,
                               ReminderMapper reminderMapper) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
        this.reminderMapper = reminderMapper;
    }

    // ------------------ CREATE ------------------

    public ReminderResponseDto createReminder(String sub, ReminderCreatetDto dto) {
        User user = findUserBySubOrThrow(sub);

        // Создаем сущность через MapStruct
        Reminder reminder = reminderMapper.toEntity(dto, user);
        Reminder saved = reminderRepository.save(reminder);

        return reminderMapper.toResponseDto(saved);
    }

    // ------------------ READ ------------------

    public ReminderResponseDto getReminder(Long id) {
        Optional<Reminder> optional = reminderRepository.findById(id);
        return optional.map(reminderMapper::toResponseDto).orElse(null);
    }

    public List<ReminderResponseDto> getAllReminders(String sub) {
        User user = findUserBySubOrThrow(sub);

        List<Reminder> userReminders = reminderRepository.findByUserId(user.getId());
        return userReminders.stream()
                .map(reminderMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    // Пагинация
    public Page<ReminderResponseDto> getRemindersPaged(String sub, Pageable pageable) {
        User user = findUserBySubOrThrow(sub);

        Specification<Reminder> spec = Specification.where(ReminderSpecification.hasUser(user.getId()));
        Page<Reminder> reminderPage = reminderRepository.findAll(spec, pageable);

        return reminderPage.map(reminderMapper::toResponseDto);
    }

    // ------------------ UPDATE ------------------

    /**
     * Обновление через MapStruct:
     *  - Находим существующую сущность.
     *  - Вызываем mаппер updateEntity(dto, existing), чтобы частично перезаписать поля.
     */
    public ReminderResponseDto updateReminder(ReminderUpdateDto dto) {
        Optional<Reminder> optional = reminderRepository.findById(dto.getId());
        if (optional.isEmpty()) {
            return null;
        }
        Reminder existing = optional.get();

        // Обновляем поля через метод MapStruct
        // (См. ниже пример updateEntity(...) в ReminderMapper)
        reminderMapper.updateEntity(dto, existing);

        Reminder saved = reminderRepository.save(existing);
        return reminderMapper.toResponseDto(saved);
    }

    // ------------------ DELETE ------------------

    public boolean deleteReminder(Long id) {
        Optional<Reminder> optional = reminderRepository.findById(id);
        if (optional.isEmpty()) {
            return false;
        }
        reminderRepository.deleteById(id);
        return true;
    }

    // ------------------ Вспомогательные методы ------------------

    private User findUserBySubOrThrow(String sub) {
        return userRepository.findBySub(sub)
                .orElseThrow(() -> new RuntimeException("User not found with sub = " + sub));
    }
}
