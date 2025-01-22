package greencity.ubstelegrambot.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.ArrayList;
import java.util.List;

public class KeyboardFactory {
    private KeyboardFactory() {
    }

    public static InlineKeyboardMarkup createHelpKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(createRow("Стартова команда", "/start"));
        keyboard.add(createRow("Допомога", "/help"));

        return InlineKeyboardMarkup
            .builder()
            .keyboard(keyboard)
            .build();
    }

    private static List<InlineKeyboardButton> createRow(String text, String callbackData) {
        var button = InlineKeyboardButton
            .builder()
            .text(text)
            .callbackData(callbackData)
            .build();
        return List.of(button);
    }
}
