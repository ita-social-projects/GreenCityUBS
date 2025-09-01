package greencity.ubstelegrambot;

import greencity.constant.TelegramBotConstants;
import greencity.entity.telegram.TelegramChat;
import greencity.enums.ChatState;
import greencity.repository.TelegramChatRepository;
import greencity.service.ubs.*;
import greencity.ubstelegrambot.messages.MessageFactory;
import greencity.ubstelegrambot.messages.MessageProvider;
import greencity.ubstelegrambot.service.TelegramUtils;
import greencity.ubstelegrambot.service.UserUpdateProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUpdateProcessorTest {

    @InjectMocks
    private UserUpdateProcessor updateProcessor;

    @Mock
    private TelegramChatRepository telegramChatRepository;

    @Mock
    private TelegramUtils telegramUtils;

    @Mock
    private TelegramFeedbackService telegramFeedbackService;

    @Mock
    private TelegramGreenOfficeService telegramGreenOfficeService;

    @Mock
    private TelegramSupportService telegramSupportService;

    @Mock
    private TelegramCommandsService telegramCommandsService;

    @Mock
    private TelegramLoginService telegramLoginService;

    @Mock
    private TelegramLanguageService telegramLanguageService;

    private static final String CHAT_ID = "123";

    @BeforeEach
    void setUp() {
        lenient().when(telegramLanguageService.getChatLanguage(anyString()))
            .thenReturn(TelegramBotConstants.UK);
    }

    @Test
    void testProcess_HasSupportCallback_MessageReturned() {
        Update update = createUpdateWithCallback(TelegramBotConstants.CLIENT_SUPPORT_CALLBACK);
        SendMessage expected = MessageFactory.createSupportMessageCallBackQuery(CHAT_ID, TelegramBotConstants.UK);

        when(telegramUtils.updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.IN_SUPPORT), any()))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramUtils).updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.IN_SUPPORT), any());
    }

    @Test
    void testProcess_HasSortingProcessCallback_MessageReturned() {
        Update update = createUpdateWithCallback(TelegramBotConstants.SORTING_PRICES_CALLBACK);
        SendMessage expected = MessageFactory.createSortingPricesMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramUtils.updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.NORMAL), any()))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramUtils).updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.NORMAL), any());
    }

    @Test
    void testProcess_HasWorkScheduleCallback_MessageReturned() {
        Update update = createUpdateWithCallback(TelegramBotConstants.WORK_SCHEDULE_CALLBACK);
        SendMessage expected = MessageFactory.createWorkScheduleMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramUtils.updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.NORMAL), any()))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramUtils).updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.NORMAL), any());
    }

    @Test
    void testProcess_HasAdmissionRulesCallback_MessageReturned() {
        Update update = createUpdateWithCallback(TelegramBotConstants.ADMISSION_RULES_CALLBACK);
        SendMessage expected = MessageFactory.createAdmissionRulesMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramUtils.updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.NORMAL), any()))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramUtils).updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.NORMAL), any());
    }

    @Test
    void testProcess_HasGreenOfficeCallback_MessageReturned() {
        Update update = createUpdateWithCallback(TelegramBotConstants.GREEN_OFFICE_CALLBACK);
        SendMessage expected = MessageFactory.createGreenOfficeMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramUtils.updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.NORMAL), any()))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramUtils).updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.NORMAL), any());
    }

    @Test
    void testProcess_HasGreenOfficeProcessCallback_MessageReturned() {
        Update update = createUpdateWithCallback(TelegramBotConstants.GREEN_OFFICE_PROCESS_CALLBACK);
        SendMessage expected = MessageFactory.createEnteringEmailMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramUtils.updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.ENTERING_GREEN_OFFICE_EMAIL), any()))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramUtils).updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.ENTERING_GREEN_OFFICE_EMAIL), any());
    }

    @Test
    void testProcess_HasFeedbackCallback_MessageReturned() {
        Update update = createUpdateWithCallback(TelegramBotConstants.FEEDBACK_CALLBACK);
        SendMessage expected = MessageFactory.createFeedbackMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramUtils.updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.NORMAL), any()))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramUtils).updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.NORMAL), any());
    }

    @Test
    void testProcess_HasRatingTerriblyCallback_MessageReturned() {
        Update update = createUpdateWithCallback(TelegramBotConstants.RATING_TERRIBLY_CALLBACK);
        SendMessage expected = MessageFactory.createBadFeedbackMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramFeedbackService.processRatingFeedbackRequest(CHAT_ID, 1))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramFeedbackService).processRatingFeedbackRequest(CHAT_ID, 1);
    }

    @Test
    void testProcess_HasRatingBadlyCallback_MessageReturned() {
        Update update = createUpdateWithCallback(TelegramBotConstants.RATING_BADLY_CALLBACK);
        SendMessage expected = MessageFactory.createBadFeedbackMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramFeedbackService.processRatingFeedbackRequest(CHAT_ID, 2))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramFeedbackService).processRatingFeedbackRequest(CHAT_ID, 2);
    }

    @Test
    void testProcess_HasRatingSatisfactorilyCallback_MessageReturned() {
        Update update = createUpdateWithCallback(TelegramBotConstants.RATING_SATISFACTORILY_CALLBACK);
        SendMessage expected = MessageFactory.createBadFeedbackMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramFeedbackService.processRatingFeedbackRequest(CHAT_ID, 3))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramFeedbackService).processRatingFeedbackRequest(CHAT_ID, 3);
    }

    @Test
    void testProcess_HasRatingGoodCallback_MessageReturned() {
        Update update = createUpdateWithCallback(TelegramBotConstants.RATING_GOOD_CALLBACK);
        SendMessage expected = MessageFactory.createGreatFeedbackMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramFeedbackService.processRatingFeedbackRequest(CHAT_ID, 4))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramFeedbackService).processRatingFeedbackRequest(CHAT_ID, 4);
    }

    @Test
    void testProcess_HasRatingPerfectlyCallback_MessageReturned() {
        Update update = createUpdateWithCallback(TelegramBotConstants.RATING_PERFECTLY_CALLBACK);
        SendMessage expected = MessageFactory.createGreatFeedbackMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramFeedbackService.processRatingFeedbackRequest(CHAT_ID, 5))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramFeedbackService).processRatingFeedbackRequest(CHAT_ID, 5);
    }

    @Test
    void testProcess_HasLoginCallback_MessageReturned() {
        Update update = createUpdateWithCallback(TelegramBotConstants.LOGIN_CALLBACK);
        SendMessage expected = MessageFactory.createLoginMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramUtils.updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.LOGGING_AS_MANAGER), any()))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramUtils).updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.LOGGING_AS_MANAGER), any());
    }

    @Test
    void testProcess_HasUnknownCallback_MessageReturned() {
        Update update = createUpdateWithCallback("UNKNOWN_CALLBACK");
        SendMessage expected = MessageFactory.createAvailableCommandsMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramUtils.updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.NORMAL), any()))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramUtils).updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.NORMAL), any());
    }

    @Test
    void testProcess_HasSupportMessageChatNotFound_MessageReturned() {
        Update update =
            createUpdateWithMessage(
                MessageProvider.get(TelegramBotConstants.UK, "client.support.message.callback.query"));
        SendMessage expected = MessageFactory.createUnknownErrorOccurredMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramChatRepository.findByChatId(CHAT_ID))
            .thenReturn(Optional.empty());

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramSupportService, never()).processSupportMessage(any(), anyString());
    }

    @Test
    void testProcess_HasSupportMessage_MessageReturned() {
        Update update =
            createUpdateWithMessage(
                MessageProvider.get(TelegramBotConstants.UK, "client.support.message.callback.query"));
        SendMessage expected = MessageFactory.createSupportMessageCallBackQuery(CHAT_ID, TelegramBotConstants.UK);

        when(telegramChatRepository.findByChatId(CHAT_ID))
            .thenReturn(Optional.of(createTelegramChat(ChatState.IN_SUPPORT)));

        when(telegramSupportService.processSupportMessage(any(), nullable(String.class)))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramSupportService).processSupportMessage(any(), nullable(String.class));
    }

    @Test
    void testProcess_HasFeedback_MessageReturned() {
        Update update =
            createUpdateWithMessage(MessageProvider.get(TelegramBotConstants.UK, "feedback.thank.you.message"));
        SendMessage expected = MessageFactory.createFeedbackThanksMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramChatRepository.findByChatId(CHAT_ID))
            .thenReturn(Optional.of(createTelegramChat(ChatState.MAKING_FEEDBACK)));

        when(telegramFeedbackService.processInputCommentRequest(any(), nullable(String.class)))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramFeedbackService).processInputCommentRequest(any(), nullable(String.class));
    }

    @Test
    void testProcess_HasGreenOfficeEmail_MessageReturned() {
        Update update =
            createUpdateWithMessage(MessageProvider.get(TelegramBotConstants.UK, "green.office.thank.you.message"));
        SendMessage expected = MessageFactory.createGreenOfficeThanksMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramChatRepository.findByChatId(CHAT_ID))
            .thenReturn(Optional.of(createTelegramChat(ChatState.ENTERING_GREEN_OFFICE_EMAIL)));

        when(telegramGreenOfficeService.processGreenOfficeEmail(any(), nullable(String.class)))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramGreenOfficeService).processGreenOfficeEmail(any(), nullable(String.class));
    }

    @Test
    void testProcess_HasCommand_MessageReturned() {
        Update update =
            createUpdateWithMessage(MessageProvider.get(TelegramBotConstants.UK, "supported.commands"));
        SendMessage expected = MessageFactory.createAvailableCommandsMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramChatRepository.findByChatId(CHAT_ID))
            .thenReturn(Optional.of(createTelegramChat(ChatState.NORMAL)));

        when(telegramCommandsService.processCommand(any(), nullable(String.class)))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramCommandsService).processCommand(any(), nullable(String.class));
    }

    @Test
    void testProcess_HasManagerCredentials_MessageReturned() {
        Update update = createUpdateWithMessage(MessageProvider.get(TelegramBotConstants.UK, "login.message"));
        SendMessage expected = MessageFactory.createFeedbackThanksMessage(CHAT_ID, TelegramBotConstants.UK);

        when(telegramChatRepository.findByChatId(CHAT_ID))
            .thenReturn(Optional.of(createTelegramChat(ChatState.LOGGING_AS_MANAGER)));

        when(telegramLoginService.processInputManagerCredentialsRequest(any(), nullable(String.class)))
            .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramLoginService).processInputManagerCredentialsRequest(any(), nullable(String.class));
    }

    private Update createUpdateWithCallback(String callbackData) {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        Message message = new Message();
        Chat chat = new Chat();

        chat.setId(Long.parseLong(CHAT_ID));
        message.setChat(chat);
        callbackQuery.setData(callbackData);
        callbackQuery.setMessage(message);
        update.setCallbackQuery(callbackQuery);
        return update;
    }

    private Update createUpdateWithMessage(String text) {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        User user = new User();

        chat.setId(Long.parseLong(CHAT_ID));
        user.setId(Long.parseLong(CHAT_ID));
        message.setChat(chat);
        message.setFrom(user);
        message.setText(text);
        update.setMessage(message);
        return update;
    }

    private TelegramChat createTelegramChat(ChatState state) {
        return TelegramChat.builder()
            .chatId(CHAT_ID)
            .chatState(state)
            .build();
    }

    private void assertMessageEquals(SendMessage expected, SendMessage actual) {
        assertNotNull(actual);
        assertEquals(expected.getText(), actual.getText());
    }
}
