package unit.com.patrick.reminder.service;

import com.patrick.reminder.dto.user.UserRequestDto;
import com.patrick.reminder.dto.user.UserResponseDto;
import com.patrick.reminder.entity.User;
import com.patrick.reminder.mapper.UserMapper;
import com.patrick.reminder.repository.UserRepository;
import com.patrick.reminder.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void createOrUpdateUser_UserNotExist() {
        // given
        UserRequestDto dto = new UserRequestDto();
        dto.setUsername("TestUser");
        dto.setEmail("test@example.com");

        // userRepository.findBySub(...) -> Optional.empty()
        when(userRepository.findBySub("test-sub")).thenReturn(Optional.empty());

        // userMapper.toEntity(dto) -> newUser
        User newUser = new User();
        newUser.setUsername("TestUser");
        when(userMapper.toEntity(dto)).thenReturn(newUser);

        User savedUser = new User();
        savedUser.setId(99L);
        savedUser.setUsername("TestUser");
        savedUser.setSub("test-sub");
        when(userRepository.save(newUser)).thenReturn(savedUser);

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(99L);
        responseDto.setSub("test-sub");
        when(userMapper.toResponseDto(savedUser)).thenReturn(responseDto);

        // when
        UserResponseDto result = userService.createOrUpdateUser("test-sub", dto);

        // then
        assertNotNull(result);
        assertEquals(99L, result.getId());
        assertEquals("test-sub", result.getSub());

        verify(userRepository).findBySub("test-sub");
        verify(userMapper).toEntity(dto);
        verify(userRepository).save(newUser);
    }

    @Test
    void createOrUpdateUser_UserExist_Update() {
        // given
        UserRequestDto dto = new UserRequestDto();
        dto.setUsername("NewName");
        dto.setEmail("new@example.com");
        dto.setTelegramId("1234");

        User existing = new User();
        existing.setId(33L);
        existing.setSub("test-sub");
        existing.setUsername("OldName");
        existing.setEmail("old@example.com");

        when(userRepository.findBySub("test-sub")).thenReturn(Optional.of(existing));

        User updated = new User();
        updated.setId(33L);
        updated.setSub("test-sub");
        updated.setUsername("NewName");
        updated.setEmail("new@example.com");
        updated.setTelegramId("1234");

        when(userRepository.save(existing)).thenReturn(updated);

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(33L);
        responseDto.setSub("test-sub");
        responseDto.setUsername("NewName");

        when(userMapper.toResponseDto(updated)).thenReturn(responseDto);

        // when
        UserResponseDto result = userService.createOrUpdateUser("test-sub", dto);

        // then
        assertEquals(33L, result.getId());
        assertEquals("test-sub", result.getSub());
        assertEquals("NewName", result.getUsername());
        verify(userRepository, times(1)).save(existing);
    }

    @Test
    void getUserById_Found() {
        // given
        User user = new User();
        user.setId(5L);
        user.setSub("user-sub");

        UserResponseDto dto = new UserResponseDto();
        dto.setId(5L);
        dto.setSub("user-sub");

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(dto);

        // when
        UserResponseDto result = userService.getUserById(5L);

        // then
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("user-sub", result.getSub());
    }

    @Test
    void getUserById_NotFound() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        UserResponseDto result = userService.getUserById(999L);

        // then
        assertNull(result);
    }

    @Test
    void deleteUserById_Success() {
        // just verifying call
        userService.deleteUserById(10L);
        verify(userRepository).deleteById(10L);
    }
}
