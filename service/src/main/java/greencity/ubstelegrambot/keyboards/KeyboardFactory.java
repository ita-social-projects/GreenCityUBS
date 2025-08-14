package greencity.ubstelegrambot.keyboards;

import com.vdurmont.emoji.EmojiParser;
import greencity.constant.TelegramBotConstants;
import greencity.ubstelegrambot.messages.MessageProvider;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.ArrayList;
import java.util.List;
import static greencity.constant.TelegramBotConstants.*;

public class KeyboardFactory {
    private KeyboardFactory() {
    }

    /**
     * Method creates InlineKeyboardMarkup for help command for user.
     *
     * @return InlineKeyboardMarkup with four buttons: start command, help command,
     *         login command and client support command.
     */
    public static InlineKeyboardMarkup createHelpKeyboard(String lang) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(
            createRow(MessageProvider.get(lang, "menu.client_support"), TelegramBotConstants.CLIENT_SUPPORT_CALLBACK));
        keyboard.add(
            createRow(MessageProvider.get(lang, "menu.work_schedule"), TelegramBotConstants.WORK_SCHEDULE_CALLBACK));
        keyboard.add(createRow(MessageProvider.get(lang, "menu.admission_rules"),
            TelegramBotConstants.ADMISSION_RULES_CALLBACK));
        keyboard
            .add(createRow(MessageProvider.get(lang, "menu.green_office"), TelegramBotConstants.GREEN_OFFICE_CALLBACK));
        keyboard.add(createRow(MessageProvider.get(lang, "menu.feedback"), TelegramBotConstants.FEEDBACK_CALLBACK));
        keyboard.add(
            createRow(MessageProvider.get(lang, "menu.sorting_prices"), TelegramBotConstants.SORTING_PRICES_CALLBACK));
        keyboard.add(createRow(MessageProvider.get(lang, "menu.login"), TelegramBotConstants.LOGIN_CALLBACK));
        keyboard.add(createLanguagesButton());
        return InlineKeyboardMarkup
            .builder()
            .keyboard(keyboard)
            .build();
    }

    private static List<InlineKeyboardButton> createLanguagesButton() {
        InlineKeyboardButton uaButton = InlineKeyboardButton.builder()
            .text("🇺🇦 Українська")
            .callbackData(SET_LANGUAGE_UA_CALLBACK)
            .build();

        InlineKeyboardButton enButton = InlineKeyboardButton.builder()
            .text("🇬🇧 English")
            .callbackData(SET_LANGUAGE_EN_CALLBACK)
            .build();
        return List.of(uaButton, enButton);
    }

    /**
     * Method creates InlineKeyboardMarkup for help command for manager.
     *
     * @return {@link InlineKeyboardMarkup} with one buttons: logout command.
     */
    public static InlineKeyboardMarkup createHelpKeyboardForManager(String lang) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(createRow(MessageProvider.get(lang, "logout.manager"), LOGOUT_MANAGER_CALLBACK));

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
    public static ReplyKeyboardMarkup userSupportKeyboard(String lang) {
        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(MessageProvider.get(lang, "client.end.support.mode"));

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
    public static InlineKeyboardMarkup createBackToMainMenuKeyboard(String lang) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard
            .add(createRow(MessageProvider.get(lang, "back.to.main.menu"), TelegramBotConstants.MAIN_MENU_CALLBACK));
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
    public static InlineKeyboardMarkup createProcessOrBackToMainMenuKeyboard(String callBackData, String lang) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(createRow(MessageProvider.get(lang, "yes.answer"), callBackData));
        keyboard
            .add(createRow(MessageProvider.get(lang, "back.to.main.menu"), TelegramBotConstants.MAIN_MENU_CALLBACK));

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
    public static InlineKeyboardMarkup createFeedbackOrBackToMainMenuKeyboard(String lang) {
        InlineKeyboardMarkup keyboard = createChatFeedbackRatingKeyboard();
        List<List<InlineKeyboardButton>> originalRows = keyboard.getKeyboard();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>(originalRows);

        keyboardRows.add(List.of(
            InlineKeyboardButton.builder()
                .text(MessageProvider.get(lang, "back.to.main.menu"))
                .callbackData(TelegramBotConstants.MAIN_MENU_CALLBACK)
                .build()));

        return InlineKeyboardMarkup.builder()
            .keyboard(keyboardRows)
            .build();
    }
}
