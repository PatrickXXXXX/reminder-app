package unit.com.patrick.reminder.service;

import com.patrick.reminder.dto.reminder.ReminderCreatetDto;
import com.patrick.reminder.dto.reminder.ReminderResponseDto;
import com.patrick.reminder.dto.reminder.ReminderUpdateDto;
import com.patrick.reminder.entity.Reminder;
import com.patrick.reminder.entity.User;
import com.patrick.reminder.mapper.ReminderMapper;
import com.patrick.reminder.repository.ReminderRepository;
import com.patrick.reminder.repository.UserRepository;
import com.patrick.reminder.service.reminder.ReminderCrudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderCrudServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReminderMapper reminderMapper;

    @InjectMocks
    private ReminderCrudService reminderCrudService;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setId(11L);
        testUser.setSub("test-sub");
    }

    @Test
    void createReminder_Success() {
        // given
        ReminderCreatetDto createDto = new ReminderCreatetDto();
        createDto.setName("TestName");
        createDto.setDescription("TestDesc");
        createDto.setRemindDate(LocalDate.of(2025, 1, 1));
        createDto.setRemindTime(LocalTime.of(10, 0));

        Reminder mockReminder = new Reminder();
        mockReminder.setId(101L);
        mockReminder.setName("TestName");

        ReminderResponseDto responseDto = new ReminderResponseDto();
        responseDto.setId(101L);
        responseDto.setName("TestName");
        responseDto.setUserSub("test-sub");

        // mock user repository: user is found
        when(userRepository.findBySub("test-sub")).thenReturn(Optional.of(testUser));
        // map from DTO -> Entity
        when(reminderMapper.toEntity(eq(createDto), eq(testUser))).thenReturn(mockReminder);
        // mock saving
        when(reminderRepository.save(mockReminder)).thenReturn(mockReminder);
        // map back to response
        when(reminderMapper.toResponseDto(mockReminder)).thenReturn(responseDto);

        // when
        ReminderResponseDto result = reminderCrudService.createReminder("test-sub", createDto);

        // then
        assertNotNull(result);
        assertEquals(101L, result.getId());
        assertEquals("TestName", result.getName());
        assertEquals("test-sub", result.getUserSub());

        verify(reminderRepository, times(1)).save(mockReminder);
        verify(userRepository, times(1)).findBySub("test-sub");
    }

    @Test
    void createReminder_UserNotFound_ThrowsException() {
        // given
        when(userRepository.findBySub("test-sub")).thenReturn(Optional.empty());
        ReminderCreatetDto createDto = new ReminderCreatetDto();

        // when & then
        assertThrows(RuntimeException.class, () ->
                reminderCrudService.createReminder("test-sub", createDto));
    }

    @Test
    void getReminder_Found() {
        // given
        Reminder entity = new Reminder();
        entity.setId(5L);

        ReminderResponseDto dto = new ReminderResponseDto();
        dto.setId(5L);

        when(reminderRepository.findById(5L)).thenReturn(Optional.of(entity));
        when(reminderMapper.toResponseDto(entity)).thenReturn(dto);

        // when
        ReminderResponseDto result = reminderCrudService.getReminder(5L);

        // then
        assertNotNull(result);
        assertEquals(5L, result.getId());
    }

    @Test
    void getReminder_NotFound() {
        // given
        when(reminderRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        ReminderResponseDto result = reminderCrudService.getReminder(999L);

        // then
        assertNull(result);
    }

    @Test
    void getAllReminders_Success() {
        // given
        when(userRepository.findBySub("test-sub")).thenReturn(Optional.of(testUser));

        Reminder r1 = new Reminder();
        r1.setId(1L);
        Reminder r2 = new Reminder();
        r2.setId(2L);

        when(reminderRepository.findByUserId(11L)).thenReturn(Arrays.asList(r1, r2));

        ReminderResponseDto dto1 = new ReminderResponseDto();
        dto1.setId(1L);
        ReminderResponseDto dto2 = new ReminderResponseDto();
        dto2.setId(2L);

        when(reminderMapper.toResponseDto(r1)).thenReturn(dto1);
        when(reminderMapper.toResponseDto(r2)).thenReturn(dto2);

        // when
        var list = reminderCrudService.getAllReminders("test-sub");

        // then
        assertEquals(2, list.size());
        assertEquals(1L, list.get(0).getId());
        assertEquals(2L, list.get(1).getId());
    }

    @Test
    void updateReminder_FoundAndUpdated() {
        // given
        ReminderUpdateDto updateDto = new ReminderUpdateDto();
        updateDto.setId(10L);
        updateDto.setName("UpdatedName");

        Reminder existingEntity = new Reminder();
        existingEntity.setId(10L);
        existingEntity.setName("OldName");

        when(reminderRepository.findById(10L)).thenReturn(Optional.of(existingEntity));

        // mapper.updateEntity(...) меняет поля existingEntity
        doAnswer(invocation -> {
            ReminderUpdateDto dtoArg = invocation.getArgument(0);
            Reminder reminderArg = invocation.getArgument(1);
            // реально меняем поля
            reminderArg.setName(dtoArg.getName());
            return null;
        }).when(reminderMapper).updateEntity(eq(updateDto), eq(existingEntity));

        Reminder savedEntity = new Reminder();
        savedEntity.setId(10L);
        savedEntity.setName("UpdatedName");

        ReminderResponseDto responseDto = new ReminderResponseDto();
        responseDto.setId(10L);
        responseDto.setName("UpdatedName");

        when(reminderRepository.save(existingEntity)).thenReturn(savedEntity);
        when(reminderMapper.toResponseDto(savedEntity)).thenReturn(responseDto);

        // when
        ReminderResponseDto result = reminderCrudService.updateReminder(updateDto);

        // then
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("UpdatedName", result.getName());
    }

    @Test
    void updateReminder_NotFound() {
        // given
        ReminderUpdateDto updateDto = new ReminderUpdateDto();
        updateDto.setId(9999L);
        when(reminderRepository.findById(9999L)).thenReturn(Optional.empty());

        // when
        ReminderResponseDto result = reminderCrudService.updateReminder(updateDto);

        // then
        assertNull(result);
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    void deleteReminder_Found() {
        // given
        Reminder existing = new Reminder();
        existing.setId(88L);

        when(reminderRepository.findById(88L)).thenReturn(Optional.of(existing));

        // when
        boolean success = reminderCrudService.deleteReminder(88L);

        // then
        assertTrue(success);
        verify(reminderRepository, times(1)).deleteById(88L);
    }

    @Test
    void deleteReminder_NotFound() {
        // given
        when(reminderRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        boolean success = reminderCrudService.deleteReminder(999L);

        // then
        assertFalse(success);
        verify(reminderRepository, never()).deleteById(anyLong());
    }
}
