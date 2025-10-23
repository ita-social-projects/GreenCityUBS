package greencity.ubstelegrambot.service;

import greencity.constant.TelegramBotConstants;
import greencity.enums.ChatState;
import greencity.enums.MessageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramCommandsServiceTest {
    @InjectMocks
    private TelegramCommandsServiceImpl telegramCommandsService;

    @Mock
    private TelegramUtils telegramUtils;

    @Mock
    private TelegramBotResponseServiceImpl telegramBotResponseService;

    @Test
    void testProcessCommand_WithNullText_ShouldReturnUnknownCommandMessage() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText(null);

        String expectedText = "text";
        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.UNKNOWN_COMMAND))
            .thenReturn(expectedText);

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.NORMAL), any(SendMessage.class)))
            .thenAnswer(inv -> inv.getArgument(2));

        SendMessage result = telegramCommandsService.processCommand(message, TelegramBotConstants.UK);

        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.UNKNOWN_COMMAND);

        assertEquals("123", result.getChatId());
        assertEquals(expectedText, result.getText());
    }

    @Test
    void testProcessCommand_WithStartCommand_ShouldReturnAvailableCommands() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/start");

        String expectedText = "text";
        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.SUPPORTED_COMMANDS))
            .thenReturn(expectedText);

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.NORMAL), any(SendMessage.class)))
            .thenAnswer(inv -> inv.getArgument(2));

        SendMessage result = telegramCommandsService.processCommand(message, TelegramBotConstants.UK);

        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.SUPPORTED_COMMANDS);

        assertEquals("123", result.getChatId());
        assertEquals(expectedText, result.getText());
    }

    @Test
    void testProcessCommand_WithHelpCommand_ShouldReturnAvailableCommands() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/help");

        String expectedText = "text";
        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.SUPPORTED_COMMANDS))
            .thenReturn(expectedText);

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.NORMAL), any(SendMessage.class)))
            .thenAnswer(inv -> inv.getArgument(2));

        SendMessage result = telegramCommandsService.processCommand(message, TelegramBotConstants.UK);

        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.SUPPORTED_COMMANDS);

        assertEquals("123", result.getChatId());
        assertEquals(expectedText, result.getText());
    }

    @Test
    void testProcessCommand_WithSupportCommand_ShouldReturnSupportMessage() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/support");

        String expectedText = "text";
        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.CLIENT_SUPPORT_MESSAGE_CALLBACK_QUERY))
            .thenReturn(expectedText);

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.IN_SUPPORT), any(SendMessage.class)))
            .thenAnswer(inv -> inv.getArgument(2));

        SendMessage result = telegramCommandsService.processCommand(message, TelegramBotConstants.UK);

        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.CLIENT_SUPPORT_MESSAGE_CALLBACK_QUERY);

        assertEquals("123", result.getChatId());
        assertEquals(expectedText, result.getText());
    }

    @Test
    void testProcessCommand_WithLoginCommand_ShouldReturnLoginMessage() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/login");

        String expectedText = "text";
        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_MESSAGE))
            .thenReturn(expectedText);

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.LOGGING_AS_MANAGER),
            any(SendMessage.class)))
            .thenAnswer(inv -> inv.getArgument(2));

        SendMessage result = telegramCommandsService.processCommand(message, TelegramBotConstants.UK);

        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.LOGIN_MESSAGE);

        assertEquals("123", result.getChatId());
        assertEquals(expectedText, result.getText());
    }

    @Test
    void testProcessCommand_WithUnknownCommand_ShouldReturnUnknownCommandMessage() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/unknown");

        String expectedText = "text";
        when(telegramBotResponseService.getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.UNKNOWN_COMMAND)).thenReturn(expectedText);

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.NORMAL), any(SendMessage.class)))
            .thenAnswer(inv -> inv.getArgument(2));

        SendMessage result = telegramCommandsService.processCommand(message, TelegramBotConstants.UK);

        verify(telegramBotResponseService).getResponseByLangAndMessageType(
            TelegramBotConstants.UK, MessageType.UNKNOWN_COMMAND);

        assertEquals("123", result.getChatId());
        assertEquals(expectedText, result.getText());
    }
}
