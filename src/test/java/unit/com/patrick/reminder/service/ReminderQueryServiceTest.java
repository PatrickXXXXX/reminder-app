package unit.com.patrick.reminder.service;

import com.patrick.reminder.dto.reminder.ReminderFilterRequestDto;
import com.patrick.reminder.dto.reminder.ReminderResponseDto;
import com.patrick.reminder.dto.reminder.ReminderSearchRequestDto;
import com.patrick.reminder.entity.Reminder;
import com.patrick.reminder.entity.User;
import com.patrick.reminder.mapper.ReminderMapper;
import com.patrick.reminder.repository.ReminderRepository;
import com.patrick.reminder.repository.UserRepository;
import com.patrick.reminder.service.reminder.ReminderQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderQueryServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private ReminderMapper reminderMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReminderQueryService queryService;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setId(11L);
        testUser.setSub("test-sub");
    }

    @Test
    void sortReminders_SimpleCase() {
        // given
        Reminder r1 = new Reminder();
        r1.setId(1L);
        r1.setName("B");

        Reminder r2 = new Reminder();
        r2.setId(2L);
        r2.setName("A");

        // Явно указываем тип в any(), чтобы выбрать findAll(Specification<Reminder>)
        when(reminderRepository.findAll(Mockito.<Specification<Reminder>>any()))
                .thenReturn(List.of(r2, r1));

        ReminderResponseDto dto1 = new ReminderResponseDto();
        dto1.setId(2L);
        dto1.setName("A");

        ReminderResponseDto dto2 = new ReminderResponseDto();
        dto2.setId(1L);
        dto2.setName("B");

        when(reminderMapper.toResponseDto(r2)).thenReturn(dto1);
        when(reminderMapper.toResponseDto(r1)).thenReturn(dto2);

        // when
        List<ReminderResponseDto> result = queryService.sortReminders("test-sub", "name");

        // then
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId()); // "A"
        assertEquals(1L, result.get(1).getId()); // "B"
    }

    @Test
    void filterReminders_SimpleCase() {
        // given
        when(userRepository.findBySub("test-sub")).thenReturn(Optional.of(testUser));

        ReminderFilterRequestDto filterDto = new ReminderFilterRequestDto();
        filterDto.setBeforeDate(LocalDate.of(2025, 1, 1));
        filterDto.setAfterTime(LocalTime.of(9, 0));

        Reminder r1 = new Reminder();
        r1.setId(100L);
        Reminder r2 = new Reminder();
        r2.setId(101L);

        // Явно указываем тип <Specification<Reminder>>
        when(reminderRepository.findAll(Mockito.<Specification<Reminder>>any()))
                .thenReturn(List.of(r1, r2));

        ReminderResponseDto dto1 = new ReminderResponseDto();
        dto1.setId(100L);
        ReminderResponseDto dto2 = new ReminderResponseDto();
        dto2.setId(101L);

        when(reminderMapper.toResponseDto(r1)).thenReturn(dto1);
        when(reminderMapper.toResponseDto(r2)).thenReturn(dto2);

        // when
        List<ReminderResponseDto> result =
                queryService.filterReminders("test-sub", filterDto);

        // then
        assertEquals(2, result.size());
        verify(reminderRepository, times(1))
                .findAll(Mockito.<Specification<Reminder>>any());
        verify(userRepository, times(1)).findBySub("test-sub");
    }

    @Test
    void filterReminders_UserNotFound() {
        // given
        when(userRepository.findBySub("unknown-sub")).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () ->
                queryService.filterReminders("unknown-sub", new ReminderFilterRequestDto()));
    }

    @Test
    void searchReminders_ByName() {
        // given
        ReminderSearchRequestDto searchDto = new ReminderSearchRequestDto();
        searchDto.setName("Test");

        Reminder r1 = new Reminder();
        r1.setId(1L);
        r1.setName("Test Reminder");

        when(userRepository.findBySub("test-sub")).thenReturn(Optional.of(testUser));
        when(reminderRepository.findAll(Mockito.<Specification<Reminder>>any()))
                .thenReturn(List.of(r1));

        ReminderResponseDto dto1 = new ReminderResponseDto();
        dto1.setId(1L);
        dto1.setName("Test Reminder");

        when(reminderMapper.toResponseDto(r1)).thenReturn(dto1);

        // when
        List<ReminderResponseDto> result = queryService.searchReminders("test-sub", searchDto);

        // then
        assertEquals(1, result.size());
        assertEquals("Test Reminder", result.get(0).getName());
    }

    @Test
    void searchReminders_ByDate() {
        // given
        ReminderSearchRequestDto searchDto = new ReminderSearchRequestDto();
        searchDto.setRemindDate(LocalDate.of(2025, 1, 10));

        Reminder r1 = new Reminder();
        r1.setId(1L);
        r1.setRemindDate(LocalDate.of(2025, 1, 10));

        when(userRepository.findBySub("test-sub")).thenReturn(Optional.of(testUser));
        when(reminderRepository.findAll(Mockito.<Specification<Reminder>>any()))
                .thenReturn(List.of(r1));

        ReminderResponseDto dto1 = new ReminderResponseDto();
        dto1.setId(1L);
        dto1.setRemindDate(LocalDate.of(2025, 1, 10));

        when(reminderMapper.toResponseDto(r1)).thenReturn(dto1);

        // when
        List<ReminderResponseDto> result = queryService.searchReminders("test-sub", searchDto);

        // then
        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2025, 1, 10), result.get(0).getRemindDate());
    }

    @Test
    void searchReminders_ByTime() {
        // given
        ReminderSearchRequestDto searchDto = new ReminderSearchRequestDto();
        searchDto.setRemindTime(LocalTime.of(10, 0));

        Reminder r1 = new Reminder();
        r1.setId(1L);
        r1.setRemindTime(LocalTime.of(10, 0));

        when(userRepository.findBySub("test-sub")).thenReturn(Optional.of(testUser));
        when(reminderRepository.findAll(Mockito.<Specification<Reminder>>any()))
                .thenReturn(List.of(r1));

        ReminderResponseDto dto1 = new ReminderResponseDto();
        dto1.setId(1L);
        dto1.setRemindTime(LocalTime.of(10, 0));

        when(reminderMapper.toResponseDto(r1)).thenReturn(dto1);

        // when
        List<ReminderResponseDto> result = queryService.searchReminders("test-sub", searchDto);

        // then
        assertEquals(1, result.size());
        assertEquals(LocalTime.of(10, 0), result.get(0).getRemindTime());
    }

    @Test
    void searchReminders_UserNotFound() {
        // given
        when(userRepository.findBySub("unknown-sub")).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () ->
                queryService.searchReminders("unknown-sub", new ReminderSearchRequestDto()));
    }
}
