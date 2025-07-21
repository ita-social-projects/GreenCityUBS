package greencity.ubstelegrambot;

import greencity.constant.TelegramBotConstants;
import greencity.entity.telegram.TelegramChat;
import greencity.enums.ChatState;
import greencity.repository.TelegramChatRepository;
import greencity.service.ubs.TelegramFeedbackService;
import greencity.service.ubs.TelegramLoginService;
import greencity.service.ubs.TelegramSupportService;
import greencity.ubstelegrambot.messages.MessageFactory;
import greencity.ubstelegrambot.service.TelegramUtils;
import greencity.ubstelegrambot.service.UserUpdateProcessor;
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
public class UserUpdateProcessorTest {

    @InjectMocks
    private UserUpdateProcessor updateProcessor;

    @Mock
    private TelegramChatRepository telegramChatRepository;

    @Mock
    private TelegramUtils telegramUtils;

    @Mock
    private TelegramFeedbackService telegramFeedbackService;

    @Mock
    private TelegramSupportService telegramSupportService;

    @Mock
    private TelegramLoginService telegramLoginService;

    private static final String CHAT_ID = "123";

    @Test
    void shouldHandleSupportCallback() {
        Update update = createUpdateWithCallback(TelegramBotConstants.CLIENT_SUPPORT_CALLBACK);
        SendMessage expected = MessageFactory.createSupportMessageCallBackQuery(CHAT_ID);

        when(telegramUtils.updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.IN_SUPPORT), any()))
                .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramUtils).updateChatStateAndRespond(eq(CHAT_ID), eq(ChatState.IN_SUPPORT), any());
    }

    @Test
    void shouldHandleRatingCallback() {
        Update update = createUpdateWithCallback(TelegramBotConstants.RATING_BADLY_CALLBACK);
        SendMessage expected = MessageFactory.createSupportMessageCallBackQuery(CHAT_ID);

        when(telegramFeedbackService.processRatingFeedbackRequest(eq(CHAT_ID), anyInt()))
                .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramFeedbackService).processRatingFeedbackRequest(eq(CHAT_ID), anyInt());
    }

    @Test
    void shouldHandleSupportTextMessage() {
        Update update = createUpdateWithMessage(TelegramBotConstants.CLIENT_SUPPORT_MESSAGE_CALL_BACK_QUERY, ChatState.IN_SUPPORT);
        SendMessage expected = MessageFactory.createSupportMessageCallBackQuery(CHAT_ID);

        when(telegramChatRepository.findByChatId(CHAT_ID))
                .thenReturn(Optional.of(createTelegramChat(ChatState.IN_SUPPORT)));

        when(telegramSupportService.processSupportMessage(any()))
                .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramSupportService).processSupportMessage(any());
    }

    @Test
    void shouldHandleFeedbackCommentMessage() {
        Update update = createUpdateWithMessage(TelegramBotConstants.FEEDBACK_THANK_YOU_MESSAGE, ChatState.MAKING_FEEDBACK);
        SendMessage expected = MessageFactory.createFeedbackThanksMessage(CHAT_ID);

        when(telegramChatRepository.findByChatId(CHAT_ID))
                .thenReturn(Optional.of(createTelegramChat(ChatState.MAKING_FEEDBACK)));

        when(telegramFeedbackService.processInputCommentRequest(any()))
                .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramFeedbackService).processInputCommentRequest(any());
    }

    @Test
    void shouldHandleManagerLoginMessage() {
        Update update = createUpdateWithMessage(TelegramBotConstants.LOGIN_MESSAGE, ChatState.LOGGING_AS_MANAGER);
        SendMessage expected = MessageFactory.createFeedbackThanksMessage(CHAT_ID);

        when(telegramChatRepository.findByChatId(CHAT_ID))
                .thenReturn(Optional.of(createTelegramChat(ChatState.LOGGING_AS_MANAGER)));

        when(telegramLoginService.processInputManagerCredentialsRequest(any()))
                .thenReturn(expected);

        SendMessage result = updateProcessor.process(update);

        assertMessageEquals(expected, result);
        verify(telegramLoginService).processInputManagerCredentialsRequest(any());
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

    private Update createUpdateWithMessage(String text, ChatState chatState) {
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
