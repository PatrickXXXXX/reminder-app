package com.patrick.reminder.mapper;


import com.patrick.reminder.dto.reminder.ReminderCreatetDto;
import com.patrick.reminder.dto.reminder.ReminderResponseDto;
import com.patrick.reminder.dto.reminder.ReminderUpdateDto;
import com.patrick.reminder.entity.Reminder;
import com.patrick.reminder.entity.User;
import org.mapstruct.*;

/**
 * Пример MapStruct-мэппера для Reminder:
 *  1) toEntity(dto, user)
 *  2) toResponseDto(entity)
 */
@Mapper(componentModel = "spring")
public interface ReminderMapper {

    // Чтобы потом @Autowired работал, нужен (componentModel = "spring").
    // Либо, если хотим вручную:
    // ReminderMapper INSTANCE = Mappers.getMapper(ReminderMapper.class);

    /**
     * Трансформируем ReminderCreateDto + User -> Reminder entity.
     * MapStruct не умеет в чистом виде "добавлять" второй параметр (User)
     * напрямую, поэтому делаем метод вручную.
     */
    @Mapping(target = "id", ignore = true) // id генерируется
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "remindDate", source = "dto.remindDate")
    @Mapping(target = "remindTime", source = "dto.remindTime")

    @Mapping(target = "sent", ignore = true) // sent по умолчанию false
    @Mapping(target = "user", source = "user") // user приходит вторым параметром
    Reminder toEntity(ReminderCreatetDto dto, User user);

    /**
     * Из Reminder entity -> в ReminderResponseDto.
     */
    @Mapping(target = "id", source = "reminder.id")
    @Mapping(target = "name", source = "reminder.name")
    @Mapping(target = "description", source = "reminder.description")
    @Mapping(target = "remindDate", source = "reminder.remindDate")
    @Mapping(target = "remindTime", source = "reminder.remindTime")
    @Mapping(target = "userId", source = "reminder.user.id")
    @Mapping(target = "userSub", source = "reminder.user.sub")
    ReminderResponseDto toResponseDto(Reminder reminder);

    // ------------------ updateEntity (fromUpdateDto) ------------------
    /**
     * «Частичное» обновление уже существующего Reminder из ReminderUpdateDto.
     * @MappingTarget позволяет не создавать новый объект, а обновлять поля «на месте».
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sent", ignore = true)
    void updateEntity(ReminderUpdateDto dto, @MappingTarget Reminder reminder);

}
