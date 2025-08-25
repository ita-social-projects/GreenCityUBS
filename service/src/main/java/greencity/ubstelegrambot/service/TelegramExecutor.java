package greencity.ubstelegrambot.service;

import static greencity.constant.ErrorMessage.TELEGRAM_INVALID_METHOD_EXCEPTION;
import static greencity.constant.ErrorMessage.TELEGRAM_NULL_METHOD_EXCEPTION;
import static greencity.constant.ErrorMessage.TELEGRAM_RECEIVE_EXCEPTION;
import static greencity.constant.ErrorMessage.TELEGRAM_SEND_EXCEPTION;
import greencity.exceptions.bots.TelegramBotExecutionException;
import greencity.ubstelegrambot.UBSTelegramBot;
import java.io.Serializable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.List;

/**
 * Wrapper class for executing telegram's bot methods.
 */
@Component
@RequiredArgsConstructor
public class TelegramExecutor {
    private final UBSTelegramBot telegramBot;

    /**
     * Method sends message to telegram user.
     *
     * @param method {@link BotApiMethod} is method to send telegram messages.
     */
    public void executeCommand(BotApiMethod<?> method) {
        executeSafely(method, TELEGRAM_SEND_EXCEPTION);
    }

    /**
     * Executes a GetFile request to retrieve a file from Telegram.
     *
     * @param method the GetFile method to execute
     * @return the retrieved File object
     */
    public File executeGetFile(GetFile method) {
        return executeSafely(method, TELEGRAM_RECEIVE_EXCEPTION);
    }

    /**
     * Sends a photo to a Telegram user.
     *
     * @param message method containing the photo and details
     */
    public void executeSendPhoto(SendPhoto message) {
        executeSafely(message, TELEGRAM_SEND_EXCEPTION);
    }

    /**
     * Sends a file to a Telegram user.
     *
     * @param message method containing the file and details
     */
    public void executeSendFile(SendDocument message) {
        executeSafely(message, TELEGRAM_SEND_EXCEPTION);
    }

    private <T extends Serializable> T executeSafely(PartialBotApiMethod<T> method, String errorMessage) {
        if (method == null) {
            throw new TelegramBotExecutionException(TELEGRAM_NULL_METHOD_EXCEPTION);
        }

        try {
            return switch (method) {
                case BotApiMethod<T> botApiMethod -> telegramBot.execute(botApiMethod);
                case SendPhoto sendPhoto -> (T) telegramBot.execute(sendPhoto);
                case SendDocument sendDocument -> (T) telegramBot.execute(sendDocument);
                default -> throw new TelegramBotExecutionException(TELEGRAM_INVALID_METHOD_EXCEPTION.formatted(
                    method.getClass().getSimpleName()));
            };
        } catch (TelegramApiException e) {
            throw new TelegramBotExecutionException(errorMessage.formatted(e.getMessage()), e);
        }
    }

    public Message executeSendMessage(SendMessage message) {
          return executeSafely(message, TELEGRAM_SEND_EXCEPTION);
    }
          
    public List<Message> executeSendMediaGroup(UBSTelegramBot bot, SendMediaGroup message) {
        try {
            return bot.execute(message);
        } catch (Exception e) {
            throw new MessageWasNotSent(e.getMessage());
        }
    }
}
