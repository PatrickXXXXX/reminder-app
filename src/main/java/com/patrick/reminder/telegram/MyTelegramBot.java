package com.patrick.reminder.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Реальный класс бота, по-прежнему наследует TelegramLongPollingBot.
 * Теперь он также реализует TelegramBotClient.
 */
@Component
public class MyTelegramBot extends TelegramLongPollingBot implements TelegramBotClient {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username:}")
    private String botUsername;

    /**
     * Вызывается при получении новых Update. Для упрощения
     * мы не будем тут ничего менять.
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            System.out.println("Got message: " + update.getMessage().getText()
                    + " from chatId: " + update.getMessage().getChatId());
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Метод интерфейса TelegramBotClient,
     * а также публичный метод нашего TelegramLongPollingBot.
     */
    @Override
    public void sendMessage(String chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);

        try {
            execute(msg); // метод execute(...) наследуется от TelegramLongPollingBot
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
