Описание
Reminder — веб-приложение на Java/Spring Boot, позволяющее создавать и управлять напоминаниями.
Основные возможности:
- Создание напоминаний (заголовок, описание, дата/время).
- Редактирование и удаление напоминаний.
- Поиск (по названию, описанию, дате, времени).
- Сортировка (по названию, дате, времени).
- Фильтрация (по дате, времени).
- Пагинация списка напоминаний.
- Уведомление на e-mail и в Telegram при наступлении времени напоминания.
- Управление пользователями (привязка к Google OAuth2 — `sub`), хранение email и telegramId.

Технологии:
- **Java 17**, **Spring Boot 3**, **Gradle**
- **PostgreSQL**, **Hibernate/JPA**, **Liquibase**
- **Spring Security** (OAuth2 Resource Server)
- **Quartz** (для планировщика задач/уведомлений)
- **Docker** и **docker-compose**
- **JUnit**, **Mockito**, **Spring Boot Test** (для тестирования)
- **Lombok** (для упрощения кода)
- **MapStruct** (для маппинга объектов)

Секреты и конфигурация
Для работы с конфиденциальными данными (такими как логины, пароли, токены и другие секреты), используйте файл `.env`. 

Инструкция по запуску проекта
1. Склонируйте репозиторий: git clone https://github.com/PatrickXXXXX/reminder-app
2. Перейдите в директорию проекта: cd reminder-app
3. Создайте файл .env: 
Скопируйте файл конфигурации .env.example в .env:
cp .env.example .env
4. Откройте файл .env и заполните его реальными значениями для вашего окружения:
MAIL_USERNAME — ваш email-адрес для отправки почты через SMTP-сервер.
MAIL_PASSWORD — пароль вашего почтового аккаунта или "пароль приложения" для сервисов с двухфакторной аутентификацией (например, Gmail).
TELEGRAM_BOT_TOKEN — токен для вашего бота в Telegram, который можно получить через BotFather.
Откройте Telegram и найдите BotFather (поиск по имени: @BotFather).
Получите токен вашего бота, который вам нужно будет указать в .env файле
(переменные TELEGRAM_BOT_TOKEN и TELEGRAM_BOT_USERNAME).
SPRING_DATASOURCE_URL — строка подключения к базе данных PostgreSQL. 
В случае использования Docker контейнеров для базы данных укажите имя контейнера базы данных, а не localhost:
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/reminder_db
Здесь db — это имя контейнера базы данных, определенное в docker-compose.yml, а 5432 — стандартный порт PostgreSQL.
SPRING_DATASOURCE_USERNAME — имя пользователя для подключения к базе данных (postgres по умолчанию).
SPRING_DATASOURCE_PASSWORD — пароль для подключения к базе данных (postgres по умолчанию).
5. Соберите проект: ./gradlew build
6. Запустите Docker с пересборкой образов:  docker-compose up --build
Docker создаст два контейнера:
db — контейнер с PostgreSQL.
app — контейнер с Spring Boot-приложением.
В логах появится информация о миграциях Liquibase и старте приложения.
7. Когда в логах увидите сообщение "Started ReminderApplication", сервер будет запущен и готов к приему запросов. 
PostgreSQL будет слушать порт 5432 (по умолчанию логин/пароль postgres/postgres).
8. Остановка работы:
Чтобы остановить работу контейнеров, нажмите Ctrl + C в терминале или используйте команду docker-compose down для полной остановки и очистки контейнеров.

Авторизация (Google OAuth2):
Все запросы к API требуют заголовок Authorization: Bearer <ID_TOKEN> — это Google ID Token.
Как получить Google ID Token (через OAuth2 Playground)
1.	Перейдите на OAuth2 Playground.
2.	Слева в «Select & authorize APIs» отметьте openid, email, profile (или воспользуйтесь «Google OAuth2 API v2»).
3.	Нажмите Authorize APIs, выберите/введите свой Google-аккаунт.
4.	После входа появится «Authorization Code». Нажмите Exchange authorization code for tokens.
5.	В «Step 2» отобразится ID Token (начинается с eyJhbGci...). В «Step 2» отобразится ID Token (начинается с eyJhbGci...). Скопируйте всю эту строку от eyJ... до конца. Это ваш Google ID Token
6.	Во всех запросах используйте заголовок: 
Authorization: Bearer eyJhbGciOiJS...
Внутри этого токена есть sub — уникальный идентификатор пользователя в Google. Приложение связывает объекты (пользователи, напоминания) именно через sub.

Настройка уведомлений через Telegram:
Чтобы получать уведомления в Telegram:
Найдите одного из ботов, показывающих ваш chat ID, например, @RawDataBot, @get_id_bot, @myidbot.
При создании/редактировании пользователя в приложении укажите ваш chat ID как `telegramId`.

Эндпоинты (REST API):
Все эндпоинты расположены под префиксом /api/v1/.
Приложение предоставляет следующие группы эндпоинтов:
•	Пользователь (User): Эндпоинты для создания, получения, обновления и удаления пользователей. 
•	Напоминания (Reminder): Эндпоинты для создания, редактирования, удаления, получения и поиска напоминаний. Также поддерживаются сортировка, фильтрация и пагинация напоминаний.
Каждый эндпоинт требует авторизации через Google OAuth2, и все запросы должны включать в заголовке Google ID Token.
Конкретные эндпоинты с примерами запросов приведены ниже.

Примеры запросов
1. Пользователь
 1.1 Создать / обновить пользователя

Invoke-RestMethod `
    -Method POST `
    -Uri "http://localhost:8080/api/v1/user/create" `
    -Headers @{
        "Authorization" = "Bearer BEARER_TOKEN"
        "Content-Type"  = "application/json"
    } `
    -Body '{
        "username": "Djanclod Terminator",
        "email": " example@gmail.com @gmail.com",
        "telegramId": "123456789"
    }'
Пользователь будет создан или обновлён по sub из токена.

1.2 Получить пользователя по ID
Invoke-RestMethod `
    -Method GET `
    -Uri "http://localhost:8080/api/v1/user/1" `
    -Headers @{
        "Authorization" = "Bearer BEARER_TOKEN"
    }
Вернёт данные пользователя id=1, если sub в базе совпадает с токеном.

1.3 Удалить пользователя (id=1)
Invoke-RestMethod `
    -Method DELETE `
    -Uri "http://localhost:8080/api/v1/user/1" `
    -Headers @{
        "Authorization" = "Bearer BEARER_TOKEN"
        "Content-Type"  = "application/json"
    }
Удаляет пользователя id=1, если его sub совпадает с токеном.

2. Напоминания (Reminder)
2.1 Создать новое напоминание
Invoke-RestMethod `
    -Method POST `
    -Uri "http://localhost:8080/api/v1/reminder/create" `
    -Headers @{
        "Authorization" = "Bearer BEARER_TOKEN"
        "Content-Type"  = "application/json"
    } `
    -Body '{
        "name": "Test reminder",
        "description": " Some description",
        "remindDate": "2025-02-10",
        "remindTime": "09:45"
    }'
Создаётся напоминание, привязанное к sub пользователя из токена.

2.2 Получить все напоминания (без пагинации)
Invoke-RestMethod `
    -Method GET `
    -Uri "http://localhost:8080/api/v1/reminder/list" `
    -Headers @{
        "Authorization" = "Bearer BEARER_TOKEN"
    } 
Вернёт все напоминания текущего пользователя.

2.3 Получить список напоминаний с пагинацией

Invoke-RestMethod `
    -Method GET `
    -Uri "http://localhost:8080/api/v1/reminder/paged?page=0&size=3" `
    -Headers @{
        "Authorization" = "Bearer BEARER_TOKEN"
    }
Возвращает объект Page с content[], totalElements, size, и т.д.

2.4 Получить напоминание по ID
Invoke-RestMethod `
    -Method GET `
    -Uri "http://localhost:8080/api/v1/reminder/10" `
    -Headers @{
        "Authorization" = "Bearer BEARER_TOKEN"
    } 
Если напоминание с ID=10 принадлежит вам, вернёт его данные, иначе 403/404.

2.5 Обновить напоминание
Invoke-RestMethod `
    -Method PUT `
    -Uri "http://localhost:8080/api/v1/reminder/update" `
    -Headers @{
        "Authorization" = "Bearer BEARER_TOKEN"
        "Content-Type" = "application/json"
    } `
    -Body '{
        "id": 10,
        "name": "Updated title",
        "description": "Updated description",
        "remindDate": "2026-01-11",
        "remindTime": "10:00"
    }' 
Обновит поля, если sub совпадает с владельцем.

2.6 Удалить напоминание
Invoke-RestMethod `
    -Method DELETE `
    -Uri "http://localhost:8080/api/v1/reminder/delete/10" `
    -Headers @{
        "Authorization" = "Bearer BEARER_TOKEN"
    } 
Удалит напоминание с ID=10, если оно принадлежит текущему пользователю.

Поиск, Сортировка, Фильтрация
2.7 Поиск напоминаний
Invoke-RestMethod `
    -Method GET `
    -Uri "http://localhost:8080/api/v1/reminder/search?name=Example&remindDate=2025-05-10" `
    -Headers @{
        "Authorization" = "Bearer BEARER_TOKEN"
    } 
Ищет напоминания по сочетанию параметров (name, description, remindDate или remindTime). Любой из параметров можно опустить.

2.8 Сортировка напоминаний
Invoke-RestMethod `
    -Method GET `
    -Uri "http://localhost:8080/api/v1/reminder/sort?by=name" `
    -Headers @{
        "Authorization" = "Bearer BEARER_TOKEN"
    }
Сортирует напоминания по name, remindDate, remindTime.

2.9 Фильтрация напоминаний
Invoke-RestMethod `
    -Method GET `
    -Uri "http://localhost:8080/api/v1/reminder/filter?beforeDate=2025-02-12" `
    -Headers @{
        "Authorization" = "Bearer BEARER_TOKEN"
    }
beforeDate — фильтрует напоминания, которые должны быть до указанной даты.
afterDate — фильтрует напоминания, которые должны быть после указанной даты.
beforeTime — фильтрует напоминания, которые должны быть до указанного времени.
afterTime — фильтрует напоминания, которые должны быть после указанного времени.

Примечание: во всех примерах замените BEARER_TOKEN на ваш реальный ID Token Google (начинается с eyJhbGci...) и корректные id в URI.

Примечания по безопасности:
•	Секреты (например, данные для SMTP и Telegram) хранятся в файле .env, который не должен попасть в репозиторий.
•	Для продакшн-окружения используйте безопасные методы хранения секретов.

На этом всё! Если возникнут вопросы — обращайтесь к автору проекта. Удачной работы!

