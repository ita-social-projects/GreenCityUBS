package greencity.ubstelegrambot.messages;

import greencity.constant.AppConstant;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class MessageFactory {
    private MessageFactory() {
    }

    /**
     * Method for creating welcome SendMessage for TelegramLongPollingBot.
     *
     * @param chatId {@link String} is telegram chat id.
     */
    public static SendMessage creatWelcomeMessage(String chatId) {
        return SendMessage
            .builder()
            .chatId(chatId)
            .text(AppConstant.TELEGRAM_GREETING_MESSAGE)
            .build();
    }
}