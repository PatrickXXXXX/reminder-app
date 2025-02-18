package functional.com.patrick.reminder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patrick.reminder.ReminderApplication;
import com.patrick.reminder.dto.reminder.ReminderFilterRequestDto;
import com.patrick.reminder.dto.reminder.ReminderResponseDto;
import com.patrick.reminder.dto.reminder.ReminderSearchRequestDto;
import com.patrick.reminder.service.reminder.ReminderQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// Импорт jwt
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * Тесты для эндпоинтов сортировки, фильтра и поиска (ReminderQueryController).
 */
@SpringBootTest(classes = ReminderApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReminderQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReminderQueryService queryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class SortRemindersTests {

        @Test
        @DisplayName("GET /api/v1/reminder/sort?by=name - сортировка по name (200 ОК)")
        void testSortReminders_ByName() throws Exception {
            ReminderResponseDto r = new ReminderResponseDto();
            r.setId(1L);
            r.setName("Sorted Name");
            r.setUserSub("test-sub");

            when(queryService.sortReminders("test-sub", "name"))
                    .thenReturn(Collections.singletonList(r));

            mockMvc.perform(get("/api/v1/reminder/sort")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build()))
                            .param("by", "name"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Sorted Name"));
        }
    }

    @Nested
    class FilterRemindersTests {

        @Test
        @DisplayName("GET /api/v1/reminder/filter?beforeDate=2025-01-01 - базовый сценарий (200 ОК)")
        void testFilterReminders() throws Exception {
            ReminderResponseDto r = new ReminderResponseDto();
            r.setId(2L);
            r.setName("Filtered Reminder");
            r.setUserSub("test-sub");

            when(queryService.filterReminders(eq("test-sub"), any(ReminderFilterRequestDto.class)))
                    .thenReturn(Collections.singletonList(r));

            mockMvc.perform(get("/api/v1/reminder/filter")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build()))
                            .param("beforeDate", "2025-01-01")
                            .param("afterDate", "2024-12-31")
                            .param("beforeTime", "15:00")
                            .param("afterTime", "08:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(2))
                    .andExpect(jsonPath("$[0].name").value("Filtered Reminder"));
        }
    }



    @Nested
    class SearchRemindersTests {

        @Test
        @DisplayName("GET /api/v1/reminder/search?name=test&remindDate=2025-01-10 - поиск по названию и дате (200 OK)")
        void testSearchRemindersByNameAndDate() throws Exception {
            ReminderResponseDto r = new ReminderResponseDto();
            r.setId(5L);
            r.setName("Test Reminder by Date");
            r.setRemindDate(LocalDate.parse("2025-01-10"));
            r.setUserSub("test-sub");

            when(queryService.searchReminders(eq("test-sub"), any(ReminderSearchRequestDto.class)))
                    .thenReturn(Collections.singletonList(r));

            mockMvc.perform(get("/api/v1/reminder/search")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build()))
                            .param("name", "test")
                            .param("remindDate", "2025-01-10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(5))
                    .andExpect(jsonPath("$[0].name").value("Test Reminder by Date"))
                    .andExpect(jsonPath("$[0].remindDate").value("2025-01-10"));
        }

        @Test
        @DisplayName("GET /api/v1/reminder/search?remindDate=2025-01-10&remindTime=10:00 - поиск по дате и времени (200 OK)")
        void testSearchRemindersByDateAndTime() throws Exception {
            ReminderResponseDto r = new ReminderResponseDto();
            r.setId(6L);
            r.setName("Test Reminder by Time");
            r.setRemindDate(LocalDate.parse("2025-01-10"));
            r.setRemindTime(LocalTime.parse("10:00"));
            r.setUserSub("test-sub");

            when(queryService.searchReminders(eq("test-sub"), any(ReminderSearchRequestDto.class)))
                    .thenReturn(Collections.singletonList(r));

            mockMvc.perform(get("/api/v1/reminder/search")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build()))
                            .param("remindDate", "2025-01-10")
                            .param("remindTime", "10:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(6))
                    .andExpect(jsonPath("$[0].name").value("Test Reminder by Time"))
                    .andExpect(jsonPath("$[0].remindDate").value("2025-01-10"))
                    .andExpect(jsonPath("$[0].remindTime").value("10:00:00")); // Учитываем формат с секундами
        }

        @Test
        @DisplayName("GET /api/v1/reminder/search с полными параметрами - поиск по названию, дате и времени (200 OK)")
        void testSearchRemindersByAllParams() throws Exception {
            ReminderResponseDto r = new ReminderResponseDto();
            r.setId(7L);
            r.setName("Complete Test Reminder");
            r.setRemindDate(LocalDate.parse("2025-01-10"));
            r.setRemindTime(LocalTime.parse("10:00"));
            r.setUserSub("test-sub");

            when(queryService.searchReminders(eq("test-sub"), any(ReminderSearchRequestDto.class)))
                    .thenReturn(Collections.singletonList(r));

            mockMvc.perform(get("/api/v1/reminder/search")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build()))
                            .param("name", "test")
                            .param("remindDate", "2025-01-10")
                            .param("remindTime", "10:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(7))
                    .andExpect(jsonPath("$[0].name").value("Complete Test Reminder"))
                    .andExpect(jsonPath("$[0].remindDate").value("2025-01-10"))
                    .andExpect(jsonPath("$[0].remindTime").value("10:00:00")); // Учитываем формат с секундами
        }
    }


    @Nested
    class SecurityTests {
        @Test
        @DisplayName("GET /api/v1/reminder/sort без авторизации -> 401 Unauthorized")
        void testUnauthorized() throws Exception {
            // Без .with(jwt()), ожидаем 401
            mockMvc.perform(get("/api/v1/reminder/sort")
                            .param("by", "name"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
