package greencity.ubstelegrambot.service;

import greencity.constant.TelegramBotConstants;
import greencity.enums.ChatState;
import greencity.ubstelegrambot.messages.MessageFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TelegramCommandsServiceTest {
    @InjectMocks
    private TelegramCommandsServiceImpl telegramCommandsService;

    @Mock
    private TelegramUtils telegramUtils;

    @Test
    void testProcessCommand_WithNullText_ShouldReturnUnknownCommandMessage() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText(null);

        SendMessage expectedMessage = MessageFactory.createUnknownCommandMessage("123");

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.NORMAL), any()))
            .thenReturn(expectedMessage);

        SendMessage result = telegramCommandsService.processCommand(message);

        assertEquals("123", result.getChatId());
        assertEquals(TelegramBotConstants.UNKNOWN_COMMAND, result.getText());
    }

    @Test
    void testProcessCommand_WithStartCommand_ShouldReturnAvailableCommands() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/start");

        SendMessage expectedMessage = MessageFactory.createAvailableCommandsMessage("123");

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.NORMAL), any()))
            .thenReturn(expectedMessage);

        SendMessage result = telegramCommandsService.processCommand(message);

        assertEquals("123", result.getChatId());
        assertEquals(TelegramBotConstants.SUPPORTED_COMMANDS, result.getText());
    }

    @Test
    void testProcessCommand_WithHelpCommand_ShouldReturnAvailableCommands() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/help");

        SendMessage expectedMessage = MessageFactory.createAvailableCommandsMessage("123");

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.NORMAL), any()))
            .thenReturn(expectedMessage);

        SendMessage result = telegramCommandsService.processCommand(message);

        assertEquals("123", result.getChatId());
        assertEquals(TelegramBotConstants.SUPPORTED_COMMANDS, result.getText());
    }

    @Test
    void testProcessCommand_WithSupportCommand_ShouldReturnSupportMessage() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/support");

        SendMessage expectedMessage = MessageFactory.createSupportMessageCallBackQuery("123");

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.IN_SUPPORT), any()))
            .thenReturn(expectedMessage);

        SendMessage result = telegramCommandsService.processCommand(message);

        assertEquals("123", result.getChatId());
        assertEquals(TelegramBotConstants.CLIENT_SUPPORT_MESSAGE_CALL_BACK_QUERY, result.getText());
    }

    @Test
    void testProcessCommand_WithLoginCommand_ShouldReturnLoginMessage() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/login");

        SendMessage expectedMessage = MessageFactory.createLoginMessage("123");

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.LOGGING_AS_MANAGER), any()))
            .thenReturn(expectedMessage);

        SendMessage result = telegramCommandsService.processCommand(message);

        assertEquals("123", result.getChatId());
        assertEquals(TelegramBotConstants.LOGIN_MESSAGE, result.getText());
    }

    @Test
    void testProcessCommand_WithUnknownCommand_ShouldReturnUnknownCommandMessage() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/unknown");

        SendMessage expectedMessage = MessageFactory.createUnknownCommandMessage("123");

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.NORMAL), any()))
            .thenReturn(expectedMessage);

        SendMessage result = telegramCommandsService.processCommand(message);

        assertEquals("123", result.getChatId());
        assertEquals(TelegramBotConstants.UNKNOWN_COMMAND, result.getText());
    }
}
