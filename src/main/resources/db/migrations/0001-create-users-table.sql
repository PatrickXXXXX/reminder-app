-- Создание таблицы users
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(255) NOT NULL,
                       sub VARCHAR(255) UNIQUE,
                       email VARCHAR(255),
                       telegram_id VARCHAR(255)
);
