package com.patrick.reminder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Profile("!test")
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Авторизация
                .authorizeHttpRequests(auth -> auth
                        // Разрешаем публично только корень, ошибки, и т.п. (по желанию)
                        .requestMatchers("/", "/error").permitAll()
                        // Остальные запросы требуют авторизации (Bearer JWT)
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                // Resource Server (JWT)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                // Отключим csrf (не обязателен для API)
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
