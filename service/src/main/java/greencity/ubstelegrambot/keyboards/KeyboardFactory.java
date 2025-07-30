package greencity.ubstelegrambot.keyboards;

import com.vdurmont.emoji.EmojiParser;
import greencity.constant.TelegramBotConstants;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.ArrayList;
import java.util.List;
import static greencity.constant.TelegramBotConstants.*;

public class KeyboardFactory {
    public static final String YES = "Так";

    private KeyboardFactory() {
    }

    /**
     * Method creates InlineKeyboardMarkup for help command for user.
     *
     * @return InlineKeyboardMarkup with four buttons: start command, help command,
     *         login command and client support command.
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
     * Method creates InlineKeyboardMarkup for help command for manager.
     *
     * @return {@link InlineKeyboardMarkup} with one buttons: logout command.
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

    /**
     * Creates a single-row inline keyboard with one button.
     *
     * @param text         {@link String} the button text
     * @param callbackData {@link String} the callback data sent when the button is
     *                     pressed
     * @return a list containing one {@link InlineKeyboardButton}
     */
    private static List<InlineKeyboardButton> createRow(String text, String callbackData) {
        var button = InlineKeyboardButton
            .builder()
            .text(text)
            .callbackData(callbackData)
            .build();
        return List.of(button);
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

    /**
     * Creates InlineKeyboardMarkup for back to the main menu.
     *
     * @return {@link InlineKeyboardMarkup} with one row containing button for
     *         returning to the main menu.
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
     * Creates InlineKeyboardMarkup for processing or backing to the main menu
     * keyboard.
     *
     * @param callBackData {@link String} is callback data.
     *
     * @return {@link InlineKeyboardMarkup} with two rows containing buttons for say
     *         yes and for returning to the main menu keyboard.
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

    /**
     * Creates an inline keyboard with feedback rating buttons and an additional
     * "Back to Main Menu" button.
     *
     * @return an {@link InlineKeyboardMarkup} containing rating options and a back
     *         button
     */
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
