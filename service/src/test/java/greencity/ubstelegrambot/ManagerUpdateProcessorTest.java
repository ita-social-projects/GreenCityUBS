package greencity.ubstelegrambot;

import greencity.constant.TelegramBotConstants;
import greencity.enums.ChatState;
import greencity.service.ubs.TelegramLanguageService;
import greencity.service.ubs.TelegramLoginService;
import greencity.ubstelegrambot.messages.MessageFactory;
import greencity.ubstelegrambot.service.ManagerUpdateProcessor;
import greencity.ubstelegrambot.service.TelegramUtils;
import org.junit.jupiter.api.BeforeEach;
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
class ManagerUpdateProcessorTest {
    @InjectMocks
    private ManagerUpdateProcessor managerUpdateProcessor;

    @Mock
    private TelegramLoginService telegramLoginService;

    @Mock
    private TelegramUtils telegramUtils;

    @Mock
    private TelegramLanguageService telegramLanguageService;

    @BeforeEach
    void setUp() {
        lenient().when(telegramLanguageService.getChatLanguage(anyString()))
            .thenReturn(TelegramBotConstants.UA);
    }

    @Test
    void testProcess_WithLogoutCallback_ShouldLogoutManagerAndReturnMainMenuMessage() {
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

        SendMessage expectedMessage = MessageFactory.createAvailableCommandsMessage(chatId, TelegramBotConstants.UA);

        when(telegramUtils.updateChatStateAndRespond(eq(chatId), eq(ChatState.NORMAL), any()))
            .thenReturn(expectedMessage);

        SendMessage actualMessage = managerUpdateProcessor.process(update);

        verify(telegramUtils).updateChatStateAndRespond(
            eq(chatId),
            eq(ChatState.NORMAL),
            eq(MessageFactory.createAvailableCommandsMessage(chatId, TelegramBotConstants.UA)));

        verify(telegramLoginService).logoutManager(chatId);
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testProcess_WithTextMessage_ShouldReturnAvailableManagerCommandsMessage() {
        String chatId = "123";
        Update update = new Update();

        Chat chat = new Chat();
        chat.setId(Long.parseLong(chatId));
        Message message = new Message();
        message.setChat(chat);
        update.setMessage(message);

        SendMessage expectedMessage =
            MessageFactory.createAvailableForManagerCommandsMessage(chatId, TelegramBotConstants.UA);

        when(telegramUtils.updateChatStateAndRespond(eq(chatId), eq(ChatState.NORMAL), any()))
            .thenReturn(expectedMessage);

        SendMessage actualMessage = managerUpdateProcessor.process(update);

        verify(telegramUtils).updateChatStateAndRespond(
            eq(chatId),
            eq(ChatState.NORMAL),
            eq(MessageFactory.createAvailableForManagerCommandsMessage(chatId, TelegramBotConstants.UA)));

        verifyNoInteractions(telegramLoginService);
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testProcess_WithOtherCallback_ShouldReturnForbiddenCommandsMessage() {
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

        SendMessage expectedMessage =
            MessageFactory.createForbiddenCommandsManagerMessage(chatId, TelegramBotConstants.UA);

        when(telegramUtils.updateChatStateAndRespond(eq(chatId), eq(ChatState.NORMAL), any()))
            .thenReturn(expectedMessage);

        SendMessage actualMessage = managerUpdateProcessor.process(update);

        verify(telegramUtils).updateChatStateAndRespond(
            eq(chatId),
            eq(ChatState.NORMAL),
            eq(MessageFactory.createForbiddenCommandsManagerMessage(chatId, TelegramBotConstants.UA)));

        verifyNoInteractions(telegramLoginService);
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testProcess_WhenUpdateIsEmpty_ShouldThrowException() {
        Update update = new Update();

        assertThrows(NullPointerException.class, () -> managerUpdateProcessor.process(update));
        verifyNoInteractions(telegramUtils, telegramLoginService);
    }

    @Test
    void testProcess_WithCallbackButNoMessage_ShouldThrowException() {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData("SOME_CALLBACK");

        update.setCallbackQuery(callbackQuery);

        assertThrows(NullPointerException.class, () -> managerUpdateProcessor.process(update));
    }
}
