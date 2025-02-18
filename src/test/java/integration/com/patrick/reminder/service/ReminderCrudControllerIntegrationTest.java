package integration.com.patrick.reminder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patrick.reminder.ReminderApplication;
import com.patrick.reminder.dto.reminder.ReminderCreatetDto;
import com.patrick.reminder.dto.reminder.ReminderResponseDto;
import com.patrick.reminder.dto.reminder.ReminderUpdateDto;
import com.patrick.reminder.service.reminder.ReminderCrudService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// Важно: импортируем SecurityMockMvcRequestPostProcessors.jwt
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest(classes = ReminderApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReminderCrudControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReminderCrudService crudService;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------- CREATE --------------------------------
    @Nested
    class CreateReminderTests {

        @Test
        @DisplayName("POST /api/v1/reminder/create - успешное создание (200 ОК)")
        void testCreateReminder_Success() throws Exception {
            ReminderCreatetDto createDto = new ReminderCreatetDto();
            createDto.setName("Test Name");
            createDto.setDescription("Test Desc");
            createDto.setRemindDate(LocalDate.of(2025, 1, 1));
            createDto.setRemindTime(LocalTime.of(10, 0));

            ReminderResponseDto responseDto = new ReminderResponseDto();
            responseDto.setId(100L);
            responseDto.setName("Test Name");
            responseDto.setDescription("Test Desc");
            responseDto.setUserSub("test-sub");

            when(crudService.createReminder(eq("test-sub"), any(ReminderCreatetDto.class)))
                    .thenReturn(responseDto);

            mockMvc.perform(post("/api/v1/reminder/create")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.name").value("Test Name"))
                    .andExpect(jsonPath("$.description").value("Test Desc"))
                    .andExpect(jsonPath("$.userSub").value("test-sub"));

            verify(crudService, times(1))
                    .createReminder(eq("test-sub"), any(ReminderCreatetDto.class));
        }
    }

    // -------------------------------- READ (GET) --------------------------------
    @Nested
    class GetReminderTests {

        @Test
        @DisplayName("GET /api/v1/reminder/{id} - найдено, владелец совпадает (200 ОК)")
        void testGetReminder_Ok() throws Exception {
            ReminderResponseDto dto = new ReminderResponseDto();
            dto.setId(1L);
            dto.setName("Name #1");
            dto.setDescription("Desc #1");
            dto.setUserSub("test-sub");

            when(crudService.getReminder(1L)).thenReturn(dto);

            mockMvc.perform(get("/api/v1/reminder/1")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Name #1"))
                    .andExpect(jsonPath("$.description").value("Desc #1"));
        }

        @Test
        @DisplayName("GET /api/v1/reminder/{id} - не найдено (404)")
        void testGetReminder_NotFound() throws Exception {
            when(crudService.getReminder(999L)).thenReturn(null);

            mockMvc.perform(get("/api/v1/reminder/999")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /api/v1/reminder/{id} - чужое (403)")
        void testGetReminder_Forbidden() throws Exception {
            ReminderResponseDto dto = new ReminderResponseDto();
            dto.setId(2L);
            dto.setUserSub("another-sub");

            when(crudService.getReminder(2L)).thenReturn(dto);

            mockMvc.perform(get("/api/v1/reminder/2")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build())))
                    .andExpect(status().isForbidden());
        }
    }

    // -------------------------------- LIST --------------------------------
    @Nested
    class ListRemindersTests {

        @Test
        @DisplayName("GET /api/v1/reminder/list - возвращает список напоминаний")
        void testListReminders() throws Exception {
            ReminderResponseDto r1 = new ReminderResponseDto();
            r1.setId(10L);
            r1.setName("Reminder #10");
            r1.setUserSub("test-sub");

            ReminderResponseDto r2 = new ReminderResponseDto();
            r2.setId(11L);
            r2.setName("Reminder #11");
            r2.setUserSub("test-sub");

            when(crudService.getAllReminders("test-sub"))
                    .thenReturn(Arrays.asList(r1, r2));

            mockMvc.perform(get("/api/v1/reminder/list")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(10))
                    .andExpect(jsonPath("$[0].name").value("Reminder #10"))
                    .andExpect(jsonPath("$[1].id").value(11));
        }
    }

    // -------------------------------- PAGED --------------------------------
    @Nested
    class PagedRemindersTests {

        @Test
        @DisplayName("GET /api/v1/reminder/paged - возвращает страницу напоминаний")
        void testPagedReminders() throws Exception {
            ReminderResponseDto r1 = new ReminderResponseDto();
            r1.setId(101L);
            r1.setName("Paged #1");
            r1.setUserSub("test-sub");

            ReminderResponseDto r2 = new ReminderResponseDto();
            r2.setId(102L);
            r2.setName("Paged #2");
            r2.setUserSub("test-sub");

            Page<ReminderResponseDto> mockPage =
                    new PageImpl<>(Arrays.asList(r1, r2), PageRequest.of(0, 10), 2);

            when(crudService.getRemindersPaged(eq("test-sub"), any(PageRequest.class)))
                    .thenReturn(mockPage);

            mockMvc.perform(get("/api/v1/reminder/paged")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build()))
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(101))
                    .andExpect(jsonPath("$.content[1].id").value(102))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(10));
        }
    }

    // -------------------------------- UPDATE --------------------------------
    @Nested
    class UpdateReminderTests {

        @Test
        @DisplayName("PUT /api/v1/reminder/update - успешное обновление (200 ОК)")
        void testUpdateReminder_Success() throws Exception {
            ReminderUpdateDto updateDto = new ReminderUpdateDto();
            updateDto.setId(5L);
            updateDto.setName("NewName");
            updateDto.setDescription("NewDesc");
            updateDto.setRemindDate(LocalDate.of(2025, 5, 5));
            updateDto.setRemindTime(LocalTime.of(12, 0));

            // Сначала getReminder(5L)
            ReminderResponseDto existing = new ReminderResponseDto();
            existing.setId(5L);
            existing.setUserSub("test-sub");

            when(crudService.getReminder(5L)).thenReturn(existing);

            // Затем updateReminder(...)
            ReminderResponseDto updated = new ReminderResponseDto();
            updated.setId(5L);
            updated.setName("NewName");
            updated.setDescription("NewDesc");
            updated.setRemindDate(LocalDate.of(2025, 5, 5));
            updated.setRemindTime(LocalTime.of(12, 0));
            updated.setUserSub("test-sub");

            when(crudService.updateReminder(any(ReminderUpdateDto.class))).thenReturn(updated);

            mockMvc.perform(put("/api/v1/reminder/update")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(5))
                    .andExpect(jsonPath("$.name").value("NewName"))
                    .andExpect(jsonPath("$.description").value("NewDesc"));
        }

        @Test
        @DisplayName("PUT /api/v1/reminder/update - reminder не найден (404)")
        void testUpdateReminder_NotFound() throws Exception {
            ReminderUpdateDto updateDto = new ReminderUpdateDto();
            updateDto.setId(999L);

            when(crudService.getReminder(999L)).thenReturn(null);

            mockMvc.perform(put("/api/v1/reminder/update")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /api/v1/reminder/update - чужое (403)")
        void testUpdateReminder_Forbidden() throws Exception {
            ReminderUpdateDto updateDto = new ReminderUpdateDto();
            updateDto.setId(6L);
            updateDto.setName("ShouldNotUpdate");

            ReminderResponseDto existing = new ReminderResponseDto();
            existing.setId(6L);
            existing.setUserSub("someone-else-sub");

            when(crudService.getReminder(6L)).thenReturn(existing);

            mockMvc.perform(put("/api/v1/reminder/update")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isForbidden());
        }
    }

    // -------------------------------- DELETE --------------------------------
    @Nested
    class DeleteReminderTests {

        @Test
        @DisplayName("DELETE /api/v1/reminder/delete/{id} - успешное удаление (200 ОК)")
        void testDeleteReminder_Success() throws Exception {
            ReminderResponseDto existing = new ReminderResponseDto();
            existing.setId(99L);
            existing.setUserSub("test-sub");

            when(crudService.getReminder(99L)).thenReturn(existing);
            when(crudService.deleteReminder(99L)).thenReturn(true);

            mockMvc.perform(delete("/api/v1/reminder/delete/99")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build())))
                    .andExpect(status().isOk());

            verify(crudService, times(1)).deleteReminder(99L);
        }

        @Test
        @DisplayName("DELETE /api/v1/reminder/delete/{id} - reminder не найден (404)")
        void testDeleteReminder_NotFound() throws Exception {
            when(crudService.getReminder(9999L)).thenReturn(null);

            mockMvc.perform(delete("/api/v1/reminder/delete/9999")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /api/v1/reminder/delete/{id} - чужое (403)")
        void testDeleteReminder_Forbidden() throws Exception {
            ReminderResponseDto existing = new ReminderResponseDto();
            existing.setId(77L);
            existing.setUserSub("another-sub");

            when(crudService.getReminder(77L)).thenReturn(existing);

            mockMvc.perform(delete("/api/v1/reminder/delete/77")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build())))
                    .andExpect(status().isForbidden());
        }
    }

    // -------------------------------- SECURITY --------------------------------
    @Nested
    class SecurityTests {
        @Test
        @DisplayName("Без авторизации -> 401 Unauthorized")
        void testUnauthorized() throws Exception {
            // Тут НЕ добавляем .with(jwt())
            mockMvc.perform(get("/api/v1/reminder/list"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
