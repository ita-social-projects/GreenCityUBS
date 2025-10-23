package greencity.ubstelegrambot.factory.keyboards;

import greencity.constant.constant.TelegramBotConstants;
import greencity.ubstelegrambot.keyboards.KeyboardFactory;
import greencity.ubstelegrambot.messages.MessageProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class KeyboardFactoryTest {

    @Test
    void createHelpKeyboard_shouldReturn7RowsWithCorrectCallbacks() {
        InlineKeyboardMarkup keyboard = KeyboardFactory.createHelpKeyboard(TelegramBotConstants.UK);

        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();
        assertEquals(8, rows.size());

        List<String> expectedCallbacks = List.of(
            TelegramBotConstants.CLIENT_SUPPORT_CALLBACK,
            TelegramBotConstants.WORK_SCHEDULE_CALLBACK,
            TelegramBotConstants.ADMISSION_RULES_CALLBACK,
            TelegramBotConstants.GREEN_OFFICE_CALLBACK,
            TelegramBotConstants.FEEDBACK_CALLBACK,
            TelegramBotConstants.SORTING_PRICES_CALLBACK,
            TelegramBotConstants.LOGIN_CALLBACK);

        for (int i = 0; i < expectedCallbacks.size(); i++) {
            assertEquals(1, rows.get(i).size());
            assertEquals(expectedCallbacks.get(i), rows.get(i).getFirst().getCallbackData());
        }
    }

    @Test
    void createHelpKeyboardForManager_shouldReturnOneLogoutButton() {
        InlineKeyboardMarkup keyboard = KeyboardFactory.createHelpKeyboardForManager(TelegramBotConstants.UK);

        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();
        assertEquals(1, rows.size());
        assertEquals(1, rows.getFirst().size());
        assertEquals(TelegramBotConstants.LOGOUT_MANAGER_CALLBACK, rows.getFirst().getFirst().getCallbackData());
    }

    @Test
    void createChatFeedbackRatingKeyboard_shouldContain5Stars() {
        InlineKeyboardMarkup keyboard = KeyboardFactory.createChatFeedbackRatingKeyboard();

        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();
        assertEquals(5, rows.size());

        List<String> expectedCallbacks = List.of(
            TelegramBotConstants.RATING_TERRIBLY_CALLBACK,
            TelegramBotConstants.RATING_BADLY_CALLBACK,
            TelegramBotConstants.RATING_SATISFACTORILY_CALLBACK,
            TelegramBotConstants.RATING_GOOD_CALLBACK,
            TelegramBotConstants.RATING_PERFECTLY_CALLBACK);

        for (int i = 0; i < expectedCallbacks.size(); i++) {
            assertEquals(expectedCallbacks.get(i), rows.get(i).getFirst().getCallbackData());
        }
    }

    @Test
    void userSupportKeyboard_shouldReturnOneRowWithResizeTrue() {
        ReplyKeyboardMarkup keyboard = KeyboardFactory.userSupportKeyboard(TelegramBotConstants.UK);

        List<KeyboardRow> rows = keyboard.getKeyboard();
        assertEquals(1, rows.size());
        assertEquals(1, rows.getFirst().size());
        assertEquals(MessageProvider.get(TelegramBotConstants.UK, "client.end.support.mode"),
            rows.getFirst().getFirst().getText());
        assertTrue(keyboard.getResizeKeyboard());
    }

    @Test
    void createBackToMainMenuKeyboard_shouldReturnOneButton() {
        InlineKeyboardMarkup keyboard = KeyboardFactory.createBackToMainMenuKeyboard(TelegramBotConstants.UK);

        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();
        assertEquals(1, rows.size());
        assertEquals(1, rows.getFirst().size());
        assertEquals(TelegramBotConstants.MAIN_MENU_CALLBACK, rows.getFirst().getFirst().getCallbackData());
    }

    @Test
    void createProcessOrBackToMainMenuKeyboard_shouldReturnTwoRows() {
        String callback = "confirm-action";

        InlineKeyboardMarkup keyboard =
            KeyboardFactory.createProcessOrBackToMainMenuKeyboard(callback, TelegramBotConstants.UK);
        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();

        assertEquals(2, rows.size());
        assertEquals(callback, rows.get(0).getFirst().getCallbackData());
        assertEquals(TelegramBotConstants.MAIN_MENU_CALLBACK, rows.get(1).getFirst().getCallbackData());
    }

    @Test
    void createFeedbackOrBackToMainMenuKeyboard_shouldAppendBackButton() {
        InlineKeyboardMarkup keyboard = KeyboardFactory.createFeedbackOrBackToMainMenuKeyboard(TelegramBotConstants.UK);
        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();

        assertEquals(6, rows.size());
        InlineKeyboardButton backBtn = rows.get(5).getFirst();
        assertEquals(MessageProvider.get(TelegramBotConstants.UK, "back.to.main.menu"), backBtn.getText());
        assertEquals(TelegramBotConstants.MAIN_MENU_CALLBACK, backBtn.getCallbackData());
    }
}
