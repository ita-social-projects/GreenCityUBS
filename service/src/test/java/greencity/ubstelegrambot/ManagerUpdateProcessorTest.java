package greencity.ubstelegrambot;

import greencity.constant.TelegramBotConstants;
import greencity.enums.ChatState;
import greencity.service.ubs.TelegramLoginService;
import greencity.ubstelegrambot.service.ManagerUpdateProcessor;
import greencity.ubstelegrambot.service.TelegramUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagerUpdateProcessorTest {
    @InjectMocks
    private ManagerUpdateProcessor managerUpdateProcessor;

    @Mock
    private TelegramLoginService telegramLoginService;

    @Mock
    private TelegramUtils telegramUtils;

    @Test
    void testProcess_WithLogoutCallback_ShouldLogoutManagerAndReturnMainMenu() {
        String chatId = "123";
        Update update = new Update();

        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(TelegramBotConstants.LOGOUT_MANAGER_CALLBACK);

        Chat chat = new Chat();
        chat.setId(Long.parseLong(chatId));
        Message message = new Message();
        message.setChat(chat);
        callbackQuery.setMessage(message);
        update.setCallbackQuery(callbackQuery);

        SendMessage expectedMessage = new SendMessage(chatId, "Some message");

        when(telegramUtils.updateChatStateAndRespond(eq(chatId), eq(ChatState.NORMAL), any()))
            .thenReturn(expectedMessage);

        SendMessage actualMessage = managerUpdateProcessor.process(update);

        verify(telegramLoginService).logoutManager(chatId);
        verify(telegramUtils).updateChatStateAndRespond(eq(chatId), eq(ChatState.NORMAL), any());
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testProcess_WithTextMessage_ShouldReturnManagerCommandsMenuWithoutLogout() {
        String chatId = "123";
        Update update = new Update();
        Chat chat = new Chat();
        chat.setId(Long.parseLong(chatId));
        Message message = new Message();
        message.setChat(chat);
        update.setMessage(message);

        SendMessage expectedMessage = new SendMessage(chatId, "Available manager commands");

        when(telegramUtils.updateChatStateAndRespond(eq(chatId), eq(ChatState.NORMAL), any()))
            .thenReturn(expectedMessage);

        SendMessage actualMessage = managerUpdateProcessor.process(update);

        verify(telegramUtils).updateChatStateAndRespond(eq(chatId), eq(ChatState.NORMAL), any());
        verifyNoInteractions(telegramLoginService);
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testProcess_WithOtherCallback_ShouldNotLogoutManagerAndReturnMainMenu() {
        String chatId = "123";
        Update update = new Update();

        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData("OTHER_CALLBACK");

        Chat chat = new Chat();
        chat.setId(Long.parseLong(chatId));
        Message message = new Message();
        message.setChat(chat);
        callbackQuery.setMessage(message);
        update.setCallbackQuery(callbackQuery);

        SendMessage expectedMessage = new SendMessage(chatId, "Main menu message");

        when(telegramUtils.updateChatStateAndRespond(eq(chatId), eq(ChatState.NORMAL), any()))
            .thenReturn(expectedMessage);

        SendMessage actualMessage = managerUpdateProcessor.process(update);

        verify(telegramUtils).updateChatStateAndRespond(eq(chatId), eq(ChatState.NORMAL), any());
        verifyNoInteractions(telegramLoginService);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testProcess_WhenUpdateIsEmpty_ShouldThrowException() {
        Update update = new Update();

        assertThrows(NullPointerException.class, () -> managerUpdateProcessor.process(update));
        verifyNoInteractions(telegramUtils, telegramLoginService);
    }
}
