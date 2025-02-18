package com.patrick.reminder.telegram;

/**
 * Интерфейс, описывающий лишь те методы, которые нужны
 * для отправки сообщений в Telegram. Можно расширить при необходимости.
 */
public interface TelegramBotClient {
    void sendMessage(String chatId, String text);
}
