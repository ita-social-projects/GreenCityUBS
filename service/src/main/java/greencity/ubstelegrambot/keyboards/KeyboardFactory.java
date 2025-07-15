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
import static greencity.constant.TelegramBotConstants.*;

public class KeyboardFactory {
    public static final String YES = "Так";

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private KeyboardFactory() {
    }

    /**
     * Creates an inline keyboard for the user help command with predefined options.
     *
     * The keyboard contains seven rows, each with a single button for user actions such as contacting support, viewing work schedules, admission rules, green office information, leaving feedback, sorting prices, and manager login.
     *
     * @return an InlineKeyboardMarkup with user help options as buttons.
     */
    public static InlineKeyboardMarkup createHelpKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(createRow("Чат з людиною", TelegramBotConstants.CLIENT_SUPPORT_CALLBACK));
        keyboard.add(createRow("Графік роботи станції", TelegramBotConstants.WORK_SCHEDULE_CALLBACK));
        keyboard.add(createRow("Правила прийому сировини", TelegramBotConstants.ADMISSION_RULES_CALLBACK));
        keyboard.add(createRow("Зелений офіс", TelegramBotConstants.GREEN_OFFICE_CALLBACK));
        keyboard.add(createRow("Залишити відгук", TelegramBotConstants.FEEDBACK_CALLBACK));
        keyboard.add(createRow("Ціни на сортування", TelegramBotConstants.SORTING_PRICES_CALLBACK));
        keyboard.add(createRow("Увійти як менеджер", TelegramBotConstants.LOGIN_CALLBACK));

        return InlineKeyboardMarkup
            .builder()
            .keyboard(keyboard)
            .build();
    }

    /**
     * Creates an inline keyboard for the manager help command with a single logout button.
     *
     * @return an {@link InlineKeyboardMarkup} containing one row with a logout button for managers.
     */
    public static InlineKeyboardMarkup createHelpKeyboardForManager() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(createRow(LOGOUT_MANAGER, LOGOUT_MANAGER_CALLBACK));

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

        keyboard.add(createRow(String.format(star, 1), RATING_TERRIBLY_CALLBACK));
        keyboard.add(createRow(String.format(star, 2), RATING_BADLY_CALLBACK));
        keyboard.add(createRow(String.format(star, 3), RATING_SATISFACTORILY_CALLBACK));
        keyboard.add(createRow(String.format(star, 4), RATING_GOOD_CALLBACK));
        keyboard.add(createRow(String.format(star, 5), RATING_PERFECTLY_CALLBACK));

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
     * Creates a reply keyboard for user support mode with a button to stop support.
     *
     * @return a ReplyKeyboardMarkup containing a single row with a button to end support mode, with keyboard resizing enabled.
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

    /****
     * Creates an inline keyboard with a single button that returns the user to the main menu.
     *
     * @return an {@link InlineKeyboardMarkup} containing one row with a main menu button.
     */
    public static InlineKeyboardMarkup createBackToMainMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(createRow(BACK_TO_MAIN_MENU, TelegramBotConstants.MAIN_MENU_CALLBACK));
        return InlineKeyboardMarkup
            .builder()
            .keyboard(keyboard)
            .build();
    }

    /**
     * Creates an inline keyboard with options to confirm an action or return to the main menu.
     *
     * The keyboard consists of two rows: the first row contains a "Yes" button with the provided callback data, and the second row contains a button to return to the main menu.
     *
     * @param callBackData the callback data to associate with the "Yes" button
     * @return an {@link InlineKeyboardMarkup} with "Yes" and "Back to Main Menu" buttons
     */
    public static InlineKeyboardMarkup createProcessOrBackToMainMenuKeyboard(String callBackData) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(createRow(YES, callBackData));
        keyboard.add(createRow(BACK_TO_MAIN_MENU, TelegramBotConstants.MAIN_MENU_CALLBACK));

        return InlineKeyboardMarkup
            .builder()
            .keyboard(keyboard)
            .build();
    }

    public static InlineKeyboardMarkup createFeedbackOrBackToMainMenuKeyboard() {
        InlineKeyboardMarkup keyboard = createChatFeedbackRatingKeyboard();
        List<List<InlineKeyboardButton>> originalRows = keyboard.getKeyboard();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>(originalRows);

        keyboardRows.add(List.of(
            InlineKeyboardButton.builder()
                .text(BACK_TO_MAIN_MENU)
                .callbackData(TelegramBotConstants.MAIN_MENU_CALLBACK)
                .build()));

        return InlineKeyboardMarkup.builder()
            .keyboard(keyboardRows)
            .build();
    }
}
