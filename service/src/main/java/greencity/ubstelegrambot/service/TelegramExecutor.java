package greencity.ubstelegrambot.service;

import greencity.exceptions.bots.MessageWasNotSent;
import greencity.ubstelegrambot.UBSTelegramBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramExecutor {
    /**
     * Method sends message to telegram user.
     *
     * @param bot    {@link TelegramLongPollingBot} is realisation of
     *               TelegramLongPollingBot.
     * @param method {@link BotApiMethod} is method to send telegram messages.
     */
    public void executeCommand(TelegramLongPollingBot bot, BotApiMethod<?> method) {
        try {
            bot.execute(method);
        } catch (TelegramApiException e) {
            throw new MessageWasNotSent(e.getMessage());
        }
    }

    /**
     * Executes a GetFile request to retrieve a file from Telegram.
     *
     * @param bot    the Telegram bot instance
     * @param method the GetFile method to execute
     * @return the retrieved File object
     * @throws MessageWasNotSent if the request fails
     */
    public File executeGetFile(TelegramLongPollingBot bot, GetFile method) {
        try {
            return bot.execute(method);
        } catch (TelegramApiException e) {
            throw new MessageWasNotSent(e.getMessage());
        }
    }

    /**
     * Sends a photo to a Telegram user.
     *
     * @param bot     the Telegram bot instance
     * @param message the SendPhoto method containing the photo and details
     * @return the Message object returned by Telegram
     * @throws MessageWasNotSent if the photo cannot be sent
     */
    public Message executeSendPhoto(UBSTelegramBot bot, SendPhoto message) {
        try {
            return bot.execute(message);
        } catch (TelegramApiException e) {
            throw new MessageWasNotSent(e.getMessage());
        }
    }
}
