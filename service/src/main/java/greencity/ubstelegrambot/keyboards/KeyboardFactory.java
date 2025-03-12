package greencity.ubstelegrambot.keyboards;

import com.vdurmont.emoji.EmojiParser;
import greencity.constant.TelegramBotConstants;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static greencity.constant.TelegramBotConstants.SCORE;

public class KeyboardFactory {
    private KeyboardFactory() {
    }

    /**
     * Method creates InlineKeyboardMarkup for help command.
     *
     * @return InlineKeyboardMarkup with four buttons: start command, help command,
     *         login command and client support command.
     */
    public static InlineKeyboardMarkup createHelpKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(createRow("Стартова команда", TelegramBotConstants.START_CALLBACK));
        keyboard.add(createRow("Допомога", TelegramBotConstants.HELP_CALLBACK));
        keyboard.add(createRow("Увійти як менеджер", TelegramBotConstants.LOGIN_CALLBACK));
        keyboard.add(createRow("Звернутися в підтримку", TelegramBotConstants.CLIENT_SUPPORT_CALLBACK));

        return InlineKeyboardMarkup
            .builder()
            .keyboard(keyboard)
            .build();
    }

    /**
     * Creates InlineKeyboardMarkup for chat feedback rating.
     *
     * @return InlineKeyboardMarkup with one row containing buttons for rating from
     *         1 to 5.
     */
    public static InlineKeyboardMarkup createChatFeedbackRatingKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        String star = EmojiParser.parseToUnicode(":star:%s");
        keyboard.add(createRow(
            List.of(String.format(star, 1), String.format(star, 2), String.format(star, 3), String.format(star, 4),
                String.format(star, 5)),
            List.of(String.format(SCORE, 1), String.format(SCORE, 2), String.format(SCORE, 3), String.format(SCORE, 4),
                String.format(SCORE, 5))));
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

    private static List<InlineKeyboardButton> createRow(List<String> text, List<String> callbackData) {
        return IntStream.range(0, text.size())
            .mapToObj(i -> InlineKeyboardButton.builder()
                .text(text.get(i))
                .callbackData(callbackData.get(i))
                .build())
            .toList();
    }

    /**
     * Creates ReplyKeyboardMarkup for user support keyboard.
     *
     * @return ReplyKeyboardMarkup with one row containing button for stopping
     *         support mode and resize keyboard flag is set to true.
     */
    public static ReplyKeyboardMarkup userSupportKeyboard() {
        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(TelegramBotConstants.CLIENT_END_SUPPORT_MODE);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(firstRow);

        return ReplyKeyboardMarkup
            .builder()
            .keyboard(keyboardRows)
            .resizeKeyboard(true)
            .build();
    }
}
