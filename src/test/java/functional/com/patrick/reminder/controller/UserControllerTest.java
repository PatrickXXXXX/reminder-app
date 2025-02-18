package functional.com.patrick.reminder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patrick.reminder.ReminderApplication;

import com.patrick.reminder.dto.user.UserRequestDto;
import com.patrick.reminder.dto.user.UserResponseDto;
import com.patrick.reminder.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserControllerTest:
 * Тестирует эндпоинты /api/v1/user/{...}.
 * Используется SpringBootTest + MockMvc + spring-security-test (для JWT).
 */
@SpringBootTest(classes = ReminderApplication.class)
@ActiveProfiles("test")

@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class CreateUserTests {

        @Test
        @DisplayName("POST /api/v1/user/create - успешное создание или обновление (200 OK)")
        void testCreateOrUpdateUser_Success() throws Exception {
            UserRequestDto dto = new UserRequestDto();
            dto.setUsername("John");
            dto.setEmail("john@example.com");
            dto.setTelegramId("john_telegram");

            UserResponseDto responseDto = new UserResponseDto();
            responseDto.setId(1L);
            responseDto.setSub("test-sub");
            responseDto.setUsername("John");
            responseDto.setEmail("john@example.com");
            responseDto.setTelegramId("john_telegram");

            when(userService.createOrUpdateUser(eq("test-sub"), any(UserRequestDto.class)))
                    .thenReturn(responseDto);

            mockMvc.perform(post("/api/v1/user/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.sub").value("test-sub"))
                    .andExpect(jsonPath("$.username").value("John"))
                    .andExpect(jsonPath("$.email").value("john@example.com"))
                    .andExpect(jsonPath("$.telegramId").value("john_telegram"));

            verify(userService, times(1))
                    .createOrUpdateUser(eq("test-sub"), any(UserRequestDto.class));
        }
    }

    @Nested
    class GetUserTests {

        @Test
        @DisplayName("GET /api/v1/user/{id} - успешно получен (200 OK)")
        void testGetUser_Success() throws Exception {
            UserResponseDto userDto = new UserResponseDto();
            userDto.setId(10L);
            userDto.setSub("test-sub");
            userDto.setUsername("Alice");
            userDto.setEmail("alice@example.com");
            userDto.setTelegramId("alice_telegram");

            when(userService.getUserById(10L)).thenReturn(userDto);

            mockMvc.perform(get("/api/v1/user/10")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.sub").value("test-sub"))
                    .andExpect(jsonPath("$.username").value("Alice"))
                    .andExpect(jsonPath("$.email").value("alice@example.com"))
                    .andExpect(jsonPath("$.telegramId").value("alice_telegram"));
        }

        @Test
        @DisplayName("GET /api/v1/user/{id} - не найден (404)")
        void testGetUser_NotFound() throws Exception {
            when(userService.getUserById(999L)).thenReturn(null);

            mockMvc.perform(get("/api/v1/user/999")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /api/v1/user/{id} - чужой sub (403)")
        void testGetUser_Forbidden() throws Exception {
            UserResponseDto userDto = new UserResponseDto();
            userDto.setId(2L);
            userDto.setSub("another-sub");
            userDto.setUsername("Bob");

            when(userService.getUserById(2L)).thenReturn(userDto);

            mockMvc.perform(get("/api/v1/user/2")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build())))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class DeleteUserTests {

        @Test
        @DisplayName("DELETE /api/v1/user/{id} - успешно удалён (200 OK)")
        void testDeleteUser_Success() throws Exception {
            UserResponseDto userDto = new UserResponseDto();
            userDto.setId(5L);
            userDto.setSub("test-sub");

            when(userService.getUserById(5L)).thenReturn(userDto);

            mockMvc.perform(delete("/api/v1/user/5")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build())))
                    .andExpect(status().isOk());

            verify(userService, times(1)).deleteUserById(5L);
        }

        @Test
        @DisplayName("DELETE /api/v1/user/{id} - не найден (404)")
        void testDeleteUser_NotFound() throws Exception {
            when(userService.getUserById(999L)).thenReturn(null);

            mockMvc.perform(delete("/api/v1/user/999")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /api/v1/user/{id} - чужой sub (403)")
        void testDeleteUser_Forbidden() throws Exception {
            UserResponseDto userDto = new UserResponseDto();
            userDto.setId(6L);
            userDto.setSub("someone-else-sub");

            when(userService.getUserById(6L)).thenReturn(userDto);

            mockMvc.perform(delete("/api/v1/user/6")
                            .with(jwt().jwt(builder -> builder.claim("sub", "test-sub").build())))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class SecurityTests {

        @Test
        @DisplayName("GET /api/v1/user/{id} - без токена (401)")
        void testUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/user/1"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/v1/user/{id} - токен невалиден (401)")
        void testInvalidToken() throws Exception {
            mockMvc.perform(delete("/api/v1/user/1")
                            .header("Authorization", "Bearer invalid"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
