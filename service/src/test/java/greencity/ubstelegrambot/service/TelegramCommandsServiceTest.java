package greencity.ubstelegrambot.service;

import greencity.constant.TelegramBotConstants;
import greencity.enums.ChatState;
import greencity.ubstelegrambot.messages.MessageFactory;
import greencity.ubstelegrambot.messages.MessageProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramCommandsServiceTest {
    @InjectMocks
    private TelegramCommandsServiceImpl telegramCommandsService;

    @Mock
    private TelegramUtils telegramUtils;

    private static MockedStatic<MessageProvider> messageProviderMock;

    @BeforeAll
    static void mockMessageProvider() {
        messageProviderMock = mockStatic(MessageProvider.class);
        messageProviderMock.when(() -> MessageProvider.get(anyString(), anyString()))
            .thenAnswer(inv -> inv.getArgument(1));
    }

    @AfterAll
    static void closeMock() {
        messageProviderMock.close();
    }

    @Test
    void testProcessCommand_WithNullText_ShouldReturnUnknownCommandMessage() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText(null);

        SendMessage expectedMessage = MessageFactory.createUnknownCommandMessage("123", TelegramBotConstants.UK);

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.NORMAL), any()))
            .thenReturn(expectedMessage);

        SendMessage result = telegramCommandsService.processCommand(message, TelegramBotConstants.UK);

        assertEquals("123", result.getChatId());
        assertEquals("unknown.command", result.getText());
    }

    @Test
    void testProcessCommand_WithStartCommand_ShouldReturnAvailableCommands() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/start");

        SendMessage expectedMessage = MessageFactory.createAvailableCommandsMessage("123", TelegramBotConstants.UK);

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.NORMAL), any()))
            .thenReturn(expectedMessage);

        SendMessage result = telegramCommandsService.processCommand(message, TelegramBotConstants.UK);

        assertEquals("123", result.getChatId());
        assertEquals(MessageProvider.get(TelegramBotConstants.UK, "supported.commands"), result.getText());
    }

    @Test
    void testProcessCommand_WithHelpCommand_ShouldReturnAvailableCommands() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/help");

        SendMessage expectedMessage = MessageFactory.createAvailableCommandsMessage("123", TelegramBotConstants.UK);

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.NORMAL), any()))
            .thenReturn(expectedMessage);

        SendMessage result = telegramCommandsService.processCommand(message, TelegramBotConstants.UK);

        assertEquals("123", result.getChatId());
        assertEquals(MessageProvider.get(TelegramBotConstants.UK, "supported.commands"), result.getText());
    }

    @Test
    void testProcessCommand_WithSupportCommand_ShouldReturnSupportMessage() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/support");

        SendMessage expectedMessage = MessageFactory.createSupportMessageCallBackQuery("123", TelegramBotConstants.UK);

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.IN_SUPPORT), any()))
            .thenReturn(expectedMessage);

        SendMessage result = telegramCommandsService.processCommand(message, TelegramBotConstants.UK);

        assertEquals("123", result.getChatId());
        assertEquals(MessageProvider.get(TelegramBotConstants.UK, "client.support.message.callback.query"),
            result.getText());
    }

    @Test
    void testProcessCommand_WithLoginCommand_ShouldReturnLoginMessage() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/login");

        SendMessage expectedMessage = MessageFactory.createLoginMessage("123", TelegramBotConstants.UK);

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.LOGGING_AS_MANAGER), any()))
            .thenReturn(expectedMessage);

        SendMessage result = telegramCommandsService.processCommand(message, TelegramBotConstants.UK);

        assertEquals("123", result.getChatId());
        assertEquals(MessageProvider.get(TelegramBotConstants.UK, "login.message"), result.getText());
    }

    @Test
    void testProcessCommand_WithUnknownCommand_ShouldReturnUnknownCommandMessage() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/unknown");

        SendMessage expectedMessage = MessageFactory.createUnknownCommandMessage("123", TelegramBotConstants.UK);

        when(telegramUtils.updateChatStateAndRespond(eq("123"), eq(ChatState.NORMAL), any()))
            .thenReturn(expectedMessage);

        SendMessage result = telegramCommandsService.processCommand(message, TelegramBotConstants.UK);

        assertEquals("123", result.getChatId());
        assertEquals(MessageProvider.get(TelegramBotConstants.UK, "unknown.command"), result.getText());
    }
}
