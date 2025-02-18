-- Создание таблицы reminders
CREATE TABLE reminders (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           description VARCHAR(4096),
                           remind_date DATE,
                           remind_time TIME,
                           sent BOOLEAN DEFAULT FALSE,
                           user_id BIGINT NOT NULL,
                           FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

