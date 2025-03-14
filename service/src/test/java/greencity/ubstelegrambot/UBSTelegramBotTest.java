package greencity.ubstelegrambot;

import greencity.constant.TelegramBotConstants;
import greencity.service.ubs.TelegramPhotoService;
import greencity.service.ubs.TelegramService;
import greencity.ubstelegrambot.messages.MessageFactory;
import greencity.ubstelegrambot.service.TelegramExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UBSTelegramBotTest {

    @Value("${greencity.bots.ubs-bot-name}")
    private String botName;

    @Value("${greencity.bots.ubs-bot-token}")
    private String botToken;

    @Mock
    private TelegramService telegramService;

    @Mock
    private TelegramExecutor executor;

    @Mock
    private TelegramPhotoService photoService;

    @InjectMocks
    private UBSTelegramBot ubsTelegramBot;

    private boolean hasMessage;
    private boolean hasText;
    private long chatId;
    private String chatIdStr;
    private long userId;
    private String userIdStr;
    private Message message;
    private SendMessage sendMessage;
    private Update update;

    @BeforeEach
    void setUp() {
        hasMessage = true;
        hasText = true;
        chatId = 123L;
        chatIdStr = String.valueOf(chatId);
        userId = 1L;
        userIdStr = String.valueOf(userId);
        message = Mockito.mock(Message.class);
        sendMessage = Mockito.mock(SendMessage.class);
        update = Mockito.mock(Update.class);
    }

    @Test
    void getBotUsernameTest() {
        String expectedResult = botName;

        String actualResult = ubsTelegramBot.getBotUsername();

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void getBotTokenTest() {
        String expectedResult = botToken;

        String actualResult = ubsTelegramBot.getBotToken();

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void onUpdateReceivedTest_WhenUpdateHasMessage_AndUpdateMessageHasText_AndTextDidNotMatchAnyCommand_AndUserIsNotInSupportMode() {
        String messageText = "some text that does not match any command";
        boolean userIsInSupportMode = false;

        when(update.hasMessage())
            .thenReturn(hasMessage);
        when(update.getMessage())
            .thenReturn(message);
        when(message.hasText())
            .thenReturn(hasText);
        when(message.getText())
            .thenReturn(messageText);
        when(message.getChatId())
            .thenReturn(chatId);
        when(telegramService.isUserInSupportMode(chatIdStr))
            .thenReturn(userIsInSupportMode);

        try (MockedStatic<MessageFactory> mockedStatic = Mockito.mockStatic(MessageFactory.class)) {
            mockedStatic.when(() -> MessageFactory.createUnknownCommandMessage(chatIdStr))
                .thenReturn(sendMessage);

            ubsTelegramBot.onUpdateReceived(update);

            verify(telegramService).processTextCommand(update);
        }
    }

    @Test
    void onUpdateReceivedTest_WhenUpdateHasMessage_AndUpdateMessageHasText_AndTextDidNotMatchAnyCommand_AndUserIsInSupportMode() {
        String messageText = "some text that does not match any command";
        boolean userIsInSupportMode = true;

        when(update.hasMessage())
            .thenReturn(hasMessage);
        when(update.getMessage())
            .thenReturn(message);
        when(message.hasText())
            .thenReturn(hasText);
        when(message.getText())
            .thenReturn(messageText);
        when(message.getChatId())
            .thenReturn(chatId);
        when(telegramService.isUserInSupportMode(chatIdStr))
            .thenReturn(userIsInSupportMode);

        try (MockedStatic<MessageFactory> mockedStatic = Mockito.mockStatic(MessageFactory.class)) {
            mockedStatic.when(() -> MessageFactory.createEndSupportMessage(chatIdStr))
                .thenReturn(sendMessage);

            ubsTelegramBot.onUpdateReceived(update);

            verify(telegramService).isUserInSupportMode(chatIdStr);
            verify(telegramService).saveManagerMessage(chatIdStr, messageText);
            verify(executor).executeCommand(ubsTelegramBot, sendMessage);
        }
    }

    @Test
    void onUpdateReceivedTest_WhenUpdateHasMessage_AndUpdateMessageHasText_AndTextIsEndSupportModeCommand() {
        String messageText = TelegramBotConstants.CLIENT_END_SUPPORT_MODE;

        when(update.hasMessage())
            .thenReturn(hasMessage);
        when(update.getMessage())
            .thenReturn(message);
        when(message.hasText())
            .thenReturn(hasText);
        when(message.getText())
            .thenReturn(messageText);
        when(message.getChatId())
            .thenReturn(chatId);
        when(telegramService.stopSupportMode(message))
            .thenReturn(sendMessage);

        ubsTelegramBot.onUpdateReceived(update);

        verify(telegramService).stopSupportMode(message);
        verify(executor).executeCommand(ubsTelegramBot, sendMessage);
    }

    @Test
    void onUpdateReceivedTest_WhenUpdateHasMessage_AndUpdateMessageHasText_AndTextIsLoginCommand() {
        String messageText = TelegramBotConstants.LOGIN_COMMAND;

        when(update.hasMessage())
            .thenReturn(hasMessage);
        when(update.getMessage())
            .thenReturn(message);
        when(message.hasText())
            .thenReturn(hasText);
        when(message.getText())
            .thenReturn(messageText);
        when(telegramService.processLoginCommand(message))
            .thenReturn(sendMessage);

        ubsTelegramBot.onUpdateReceived(update);

        verify(telegramService).processLoginCommand(message);
        verify(executor).executeCommand(ubsTelegramBot, sendMessage);
    }

    @Test
    void onUpdateReceivedTest_WhenUpdateHasMessage_AndUpdateMessageHasText_AndTextIsSupportCommand() {
        String messageText = TelegramBotConstants.SUPPORT_COMMAND;

        when(update.hasMessage())
            .thenReturn(hasMessage);
        when(update.getMessage())
            .thenReturn(message);
        when(message.hasText())
            .thenReturn(hasText);
        when(message.getText())
            .thenReturn(messageText);
        when(telegramService.processSupportCommand(message))
            .thenReturn(sendMessage);

        ubsTelegramBot.onUpdateReceived(update);

        verify(telegramService).processSupportCommand(message);
        verify(executor).executeCommand(ubsTelegramBot, sendMessage);
    }

    @Test
    void onUpdateReceivedTest_WhenUpdateHasMessage_AndUpdateMessageHasText_AndTextIsHelpCommand() {
        String messageText = TelegramBotConstants.HELP_COMMAND;

        when(update.hasMessage())
            .thenReturn(hasMessage);
        when(update.getMessage())
            .thenReturn(message);
        when(message.hasText())
            .thenReturn(hasText);
        when(message.getText())
            .thenReturn(messageText);
        when(message.getChatId())
            .thenReturn(chatId);

        try (MockedStatic<MessageFactory> mockedStatic = Mockito.mockStatic(MessageFactory.class)) {
            mockedStatic.when(() -> MessageFactory.createHelpMessage(chatIdStr))
                .thenReturn(sendMessage);

            ubsTelegramBot.onUpdateReceived(update);

            verify(executor).executeCommand(ubsTelegramBot, sendMessage);
        }
    }

    @Test
    void onUpdateReceivedTest_WhenUpdateHasMessage_AndUpdateMessageHasText_AndTextIsStartCommand() {
        String messageText = "/start";

        when(update.hasMessage())
            .thenReturn(hasMessage);
        when(update.getMessage())
            .thenReturn(message);
        when(message.hasText())
            .thenReturn(hasText);
        when(message.getText())
            .thenReturn(messageText);
        when(telegramService.processStartCommand(message))
            .thenReturn(sendMessage);

        ubsTelegramBot.onUpdateReceived(update);

        verify(telegramService).processStartCommand(message);
        verify(executor).executeCommand(ubsTelegramBot, sendMessage);
    }

    @Test
    void onUpdateReceivedTest_WhenUpdateHasMessage_AndUpdateMessageHasPhoto() {
        boolean hasPhoto = true;
        String messageCaption = "caption";
        List<String> downloadedPhotoFromTelegram = List.of(
            "photo1",
            "photo2");

        when(update.hasMessage())
            .thenReturn(hasMessage);
        when(update.getMessage())
            .thenReturn(message);
        when(message.hasPhoto())
            .thenReturn(hasPhoto);
        when(message.getCaption())
            .thenReturn(messageCaption);
        when(message.getChatId())
            .thenReturn(chatId);

        when(photoService.downloadPhotoFromTelegram(message))
            .thenReturn(downloadedPhotoFromTelegram);

        ubsTelegramBot.onUpdateReceived(update);

        verify(photoService).downloadPhotoFromTelegram(message);
        verify(photoService).saveToDB(
            downloadedPhotoFromTelegram,
            chatIdStr,
            messageCaption);
    }

    @Test
    void onUpdateReceivedTest_WhenUpdateHasCallbackQuery_AndCallbackQueryDataEqualsClientSupportCallback() {
        hasMessage = false;
        boolean hasCallbackQuery = true;
        CallbackQuery callbackQuery = Mockito.mock(CallbackQuery.class);
        String callbackQueryData = TelegramBotConstants.CLIENT_SUPPORT_CALLBACK;
        User user = Mockito.mock(User.class);

        when(update.hasMessage())
            .thenReturn(hasMessage);
        when(update.hasCallbackQuery())
            .thenReturn(hasCallbackQuery);
        when(update.getCallbackQuery())
            .thenReturn(callbackQuery);
        when(callbackQuery.getData())
            .thenReturn(callbackQueryData);
        try (MockedStatic<MessageFactory> mockedStatic = Mockito.mockStatic(MessageFactory.class)) {
            when(callbackQuery.getFrom())
                .thenReturn(user);
            when(user.getId())
                .thenReturn(userId);
            mockedStatic.when(() -> MessageFactory.createClientSupportMessage(userIdStr))
                .thenReturn(sendMessage);

            ubsTelegramBot.onUpdateReceived(update);

            verify(executor).executeCommand(ubsTelegramBot, sendMessage);
        }
    }

    @Test
    void onUpdateReceivedTest_WhenUpdateHasCallbackQuery_AndCallbackQueryDataEqualsLoginCallback() {
        hasMessage = false;
        boolean hasCallbackQuery = true;
        CallbackQuery callbackQuery = Mockito.mock(CallbackQuery.class);
        String callbackQueryData = TelegramBotConstants.LOGIN_CALLBACK;
        User user = Mockito.mock(User.class);

        when(update.hasMessage())
            .thenReturn(hasMessage);
        when(update.hasCallbackQuery())
            .thenReturn(hasCallbackQuery);
        when(update.getCallbackQuery())
            .thenReturn(callbackQuery);
        when(callbackQuery.getData())
            .thenReturn(callbackQueryData);
        try (MockedStatic<MessageFactory> mockedStatic = Mockito.mockStatic(MessageFactory.class)) {
            when(callbackQuery.getFrom())
                .thenReturn(user);
            when(user.getId())
                .thenReturn(userId);
            mockedStatic.when(() -> MessageFactory.createLoginMessage(userIdStr))
                .thenReturn(sendMessage);

            ubsTelegramBot.onUpdateReceived(update);

            verify(executor).executeCommand(ubsTelegramBot, sendMessage);
        }
    }

    @Test
    void onUpdateReceivedTest_WhenUpdateHasCallbackQuery_AndCallbackQueryDataStartsWithScore() {
        hasMessage = false;
        boolean hasCallbackQuery = true;
        CallbackQuery callbackQuery = Mockito.mock(CallbackQuery.class);
        String callbackQueryData = TelegramBotConstants.SCORE + "score";
        User user = Mockito.mock(User.class);

        when(update.hasMessage())
            .thenReturn(hasMessage);
        when(update.hasCallbackQuery())
            .thenReturn(hasCallbackQuery);
        when(update.getCallbackQuery())
            .thenReturn(callbackQuery);
        when(callbackQuery.getData())
            .thenReturn(callbackQueryData);
        when(callbackQuery.getFrom())
            .thenReturn(user);
        when(user.getId())
            .thenReturn(userId);
        when(telegramService.handleUserChatScope(
            callbackQueryData,
            userIdStr, message.getMessageId())).thenReturn(sendMessage);

        ubsTelegramBot.onUpdateReceived(update);

        verify(telegramService).handleUserChatScope(
            callbackQuery.getData(),
            callbackQuery.getFrom().getId().toString(),
            message.getMessageId());
        verify(executor).executeCommand(ubsTelegramBot, sendMessage);
    }
}
