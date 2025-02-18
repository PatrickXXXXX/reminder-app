package unit.com.patrick.reminder.mapper;

import com.patrick.reminder.dto.reminder.ReminderUpdateDto;
import com.patrick.reminder.entity.Reminder;
import com.patrick.reminder.mapper.ReminderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class ReminderMapperTest {

    private final ReminderMapper mapper = Mappers.getMapper(ReminderMapper.class);

    @Test
    void updateEntity_shouldOverwriteSomeFields() {
        // given
        ReminderUpdateDto dto = new ReminderUpdateDto();
        dto.setId(99L); // мы игнорируем это поле при update
        dto.setName("NewName");
        dto.setDescription("NewDesc");
        // remindDate/time тоже можно проверить

        Reminder existing = new Reminder();
        existing.setId(10L);
        existing.setName("OldName");
        existing.setDescription("OldDesc");
        existing.setSent(false);

        // when
        mapper.updateEntity(dto, existing);

        // then
        // Проверяем, что id НЕ изменился (ignore = true)
        assertEquals(10L, existing.getId());
        // Проверяем, что name и description обновились
        assertEquals("NewName", existing.getName());
        assertEquals("NewDesc", existing.getDescription());
        // Проверяем, что sent НЕ перезаписан
        assertFalse(existing.isSent());
    }
}
