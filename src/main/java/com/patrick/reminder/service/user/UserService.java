package com.patrick.reminder.service.user;

import com.patrick.reminder.dto.user.UserRequestDto;
import com.patrick.reminder.dto.user.UserResponseDto;
import com.patrick.reminder.entity.User;
import com.patrick.reminder.mapper.UserMapper;
import com.patrick.reminder.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper; // <-- MapStruct-интерфейс

    public UserService(UserRepository userRepository,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponseDto createOrUpdateUser(String sub, UserRequestDto dto) {
        // Ищем, есть ли уже пользователь с таким sub
        Optional<User> existingOpt = userRepository.findBySub(sub);

        User user;
        if (existingOpt.isEmpty()) {
            // Создаём нового
            user = userMapper.toEntity(dto);
            user.setSub(sub);
        } else {
            // Обновляем
            user = existingOpt.get();
            user.setUsername(dto.getUsername());
            user.setEmail(dto.getEmail());
            user.setTelegramId(dto.getTelegramId());
        }

        User saved = userRepository.save(user);
        return userMapper.toResponseDto(saved);
    }

    public UserResponseDto getUserById(Long id) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return null;
        }
        return userMapper.toResponseDto(opt.get());
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }
}
