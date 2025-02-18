package com.patrick.reminder.mapper;


import com.patrick.reminder.dto.user.UserRequestDto;
import com.patrick.reminder.dto.user.UserResponseDto;
import com.patrick.reminder.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Из DTO -> в сущность User.
     * sub мы отдельно проставляем в сервисе, поэтому
     * сами sub здесь можно игнорировать, либо не игнорировать — по ситуации.
     */
    @Mapping(target = "id", ignore = true) // первичный ключ генерируется
    @Mapping(target = "sub", ignore = true) // sub проставим отдельно, если нужно
    @Mapping(target = "username", source = "dto.username")
    @Mapping(target = "email", source = "dto.email")
    @Mapping(target = "telegramId", source = "dto.telegramId")
    User toEntity(UserRequestDto dto);

    /**
     * Из сущности -> DTO
     */
    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "sub", source = "user.sub")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "telegramId", source = "user.telegramId")
    UserResponseDto toResponseDto(User user);

}
