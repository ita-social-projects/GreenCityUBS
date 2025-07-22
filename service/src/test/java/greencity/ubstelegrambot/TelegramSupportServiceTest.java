package greencity.ubstelegrambot;

import greencity.constant.TelegramBotConstants;
import greencity.entity.telegram.TelegramChat;
import greencity.enums.ChatState;
import greencity.repository.TelegramChatRepository;
import greencity.repository.TelegramMessageRepository;
import greencity.service.ubs.TelegramNotificationService;
import greencity.ubstelegrambot.service.TelegramExecutor;
import greencity.ubstelegrambot.service.TelegramSupportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.User;
import java.util.List;
import java.util.Optional;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramSupportServiceTest {

    @InjectMocks
    private TelegramSupportServiceImpl telegramSupportService;

    @Mock
    private TelegramChatRepository telegramChatRepository;

    @Mock
    private TelegramNotificationService telegramNotificationService;

    @Mock
    private TelegramMessageRepository telegramMessageRepository;

    @Mock
    private UBSTelegramBot bot;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private TelegramExecutor executor;;

    @BeforeEach
    void setup() {
        when(applicationContext.getBean(UBSTelegramBot.class)).thenReturn(bot);
    }

    @Test
    void testProcessSupportMessage_UnknownChat_ShouldReturnUnknownError() {
        Message message = mock(Message.class);
        User user = mock(User.class);

        when(user.getId()).thenReturn(1L);
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(1L);
        when(telegramChatRepository.findByChatId("1")).thenReturn(Optional.empty());

        SendMessage result = telegramSupportService.processSupportMessage(message);

        assertEquals(TelegramBotConstants.UNKNOWN_ERROR_OCCURRED_PLEASE_TRY_AGAIN,
                result.getText());
    }

    @Test
    void testProcessSupportMessage_EndSupportModeTextMessage_ShouldReturnStopSupportModeTextMessage() {
        TelegramChat chatEntity = TelegramChat.builder()
                .chatId("1")
                .chatState(ChatState.IN_SUPPORT)
                .build();

        Message message = mock(Message.class);
        User user = mock(User.class);

        when(user.getId()).thenReturn(1L);
        when(message.getFrom()).thenReturn(user);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn(TelegramBotConstants.CLIENT_END_SUPPORT_MODE);
        when(telegramChatRepository.findByChatId("1")).thenReturn(Optional.of(chatEntity));

        SendMessage result = telegramSupportService.processSupportMessage(message);

        assertEquals(TelegramBotConstants.CLIENT_STOP_SUPPORT_MODE, result.getText());
        verify(telegramChatRepository).save(chatEntity);
    }

    @Test
    void testProcessSupportMessage_NoLargestPhoto_ShouldReturnSomethingWentWrongMessage() {
        TelegramChat chat = TelegramChat.builder().chatId("1").build();

        PhotoSize smallPhoto = new PhotoSize(); smallPhoto.setFileSize(0);
        List<PhotoSize> photos = List.of(smallPhoto);

        Message message = mock(Message.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(1L);
        when(message.hasPhoto()).thenReturn(true);
        when(message.getPhoto()).thenReturn(photos);
        when(telegramChatRepository.findByChatId("1")).thenReturn(Optional.of(chat));

        SendMessage result = telegramSupportService.processSupportMessage(message);

        assertTrue(result.getText().contains(TelegramBotConstants.SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN));
    }

    @Test
    void testProcessSupportMessage_PhotoPassed_ErrorWhileLoading() throws Exception {
        TelegramChat chat = TelegramChat.builder().chatId("1").build();

        PhotoSize photo = new PhotoSize(); photo.setFileSize(100); photo.setFileId("file123");
        List<PhotoSize> photos = List.of(photo);

        Message message = mock(Message.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(1L);
        when(message.hasPhoto()).thenReturn(true);
        when(message.getPhoto()).thenReturn(photos);

        when(executor.executeGetFile(any(), any())).thenThrow(new RuntimeException("Connection failed"));
        when(telegramChatRepository.findByChatId("1")).thenReturn(Optional.of(chat));

        SendMessage result = telegramSupportService.processSupportMessage(message);

        assertTrue(result.getText().contains(TelegramBotConstants.SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN));
    }

    @Test
    void testProcessSupportMessage_TextMessage_ShouldReturnSentToManagerMessage() {
        TelegramChat chat = TelegramChat.builder().chatId("1").build();

        Message message = mock(Message.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(message.getFrom()).thenReturn(user);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("Hello");

        when(telegramChatRepository.findByChatId("1")).thenReturn(Optional.of(chat));

        SendMessage result = telegramSupportService.processSupportMessage(message);

        assertTrue(result.getText().contains(TelegramBotConstants.MESSAGE_SENT_TO_MANAGER_WAIT_FOR_RESPONSE));
        verify(telegramMessageRepository).save(any());
    }
}
