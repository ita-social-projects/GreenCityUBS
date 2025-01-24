package greencity.ubstelegrambot;

import greencity.exceptions.bots.MessageWasNotSent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
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
}
