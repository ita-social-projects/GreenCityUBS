package greencity.ubstelegrambot.keyboards;

import com.vdurmont.emoji.EmojiParser;
import greencity.constant.TelegramBotConstants;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.ArrayList;
import java.util.List;
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

        keyboard.add(createRow("Чат з людиною", TelegramBotConstants.CLIENT_SUPPORT_CALLBACK));
        keyboard.add(createRow("Досортування", TelegramBotConstants.SORTING_CALLBACK));
        keyboard.add(createRow("Графік роботи станції", TelegramBotConstants.WORK_SCHEDULE_CALLBACK));
        keyboard.add(createRow("Правила прийому сировини", TelegramBotConstants.ADMISSION_RULES_CALLBACK));
        keyboard.add(createRow("Зелений офіс", TelegramBotConstants.GREEN_OFFICE_CALLBACK));
        keyboard.add(createRow("Залишити відгук", TelegramBotConstants.FEEDBACK_CALLBACK));
        keyboard.add(createRow("Увійти як менеджер", TelegramBotConstants.LOGIN_CALLBACK));

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

    public static InlineKeyboardMarkup createBackToMainManuButton() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(createRow("⏪\uFE0F В головне меню", TelegramBotConstants.MAIN_MENU_CALLBACK));
        return InlineKeyboardMarkup
            .builder()
            .keyboard(keyboard)
            .build();
    }

    public static InlineKeyboardMarkup createProcessOrBackToMainMenuKeyboard(String callBackData) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(createRow("Так", callBackData));
        keyboard.add(createRow("⏪\uFE0F В головне меню", TelegramBotConstants.MAIN_MENU_CALLBACK));

        return InlineKeyboardMarkup
            .builder()
            .keyboard(keyboard)
            .build();
    }

    public static InlineKeyboardMarkup createFeedbackOrBackToMainMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(createRow("\uD83D\uDC4D Все було супер!", TelegramBotConstants.GREAT_FEEDBACK_CALLBACK));
        keyboard.add(createRow("\uD83D\uDC4E Були нюанси...\n", TelegramBotConstants.BAD_FEEDBACK_CALLBACK));
        keyboard.add(createRow("⏪\uFE0F В головне меню", TelegramBotConstants.MAIN_MENU_CALLBACK));

        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard)
                .build();
    }
}
