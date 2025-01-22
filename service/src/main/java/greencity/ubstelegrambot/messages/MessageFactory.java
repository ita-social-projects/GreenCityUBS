package greencity.ubstelegrambot.messages;

import greencity.ubstelegrambot.keyboards.KeyboardFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class MessageFactory {
    private MessageFactory() {
    }

    public static SendMessage creatWelcomeMessage(String chatId) {
        return SendMessage
            .builder()
            .chatId(chatId)
            .text("Вітаємо!\nВи підписались на UbsBot")
            .build();
    }

    public static SendMessage createHelpMessage(String chatId) {
        return SendMessage
            .builder()
            .chatId(chatId)
            .replyMarkup(KeyboardFactory.createHelpKeyboard())
            .text("Список доступних команд: ")
            .build();
    }
}
