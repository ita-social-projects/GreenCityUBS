package greencity.ubstelegrambot.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class MessageFactory {
    private MessageFactory() {}

    public static SendMessage creatWelcomeMessage(String chatId) {
        return SendMessage
                .builder()
                .chatId(chatId)
                .text("Вітаємо!\nВи підписались на UbsBot")
                .build();
    }
}