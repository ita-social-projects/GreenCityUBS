package greencity.ubstelegrambot;

import greencity.constant.TelegramBotConstants;
import greencity.dto.telegram.TelegramMessageDto;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.telegram.TelegramMessage;
import greencity.enums.ChatState;
import greencity.repository.TelegramChatRepository;
import greencity.repository.TelegramMessageRepository;
import greencity.service.ubs.FileService;
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
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.User;

import java.net.URI;
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
    private FileService fileService;

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
    void testProcessSupportMessage_UnknownChat_ShouldReturnUnknownErrorOccurredMessage() {
        Message message = mock(Message.class);
        User user = mock(User.class);

        when(message.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        when(telegramChatRepository.findByChatId(anyString())).thenReturn(Optional.empty());

        SendMessage result = telegramSupportService.processSupportMessage(message);

        assertEquals(TelegramBotConstants.UNKNOWN_ERROR_OCCURRED_PLEASE_TRY_AGAIN,
                result.getText());
    }

    @Test
    void testProcessSupportMessage_EndSupportModeTextMessage_ShouldReturnStopSupportModeTextMessage() {
        Message message = mock(Message.class);
        User user = mock(User.class);
        String chatId = "1";
        String username = "tg_user";

        when(user.getId()).thenReturn(1L);
        when(user.getUserName()).thenReturn(username);
        when(message.getFrom()).thenReturn(user);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn(TelegramBotConstants.CLIENT_END_SUPPORT_MODE);

        TelegramChat chatEntity = TelegramChat.builder()
                .chatId(chatId)
                .build();

        when(telegramChatRepository.findByChatId(chatId)).thenReturn(Optional.of(chatEntity));

        SendMessage result = telegramSupportService.processSupportMessage(message);

        assertEquals(TelegramBotConstants.CLIENT_STOP_SUPPORT_MODE, result.getText());
        verify(telegramChatRepository).save(chatEntity);
        verify(telegramNotificationService).notifyManagerAboutEndSupportModeFromUser(username);
    }

    @Test
    void testProcessSupportMessage_OnlyTextMessage_ShouldReturnSentToManagerMessage() {
        long id = 1L;
        String chatId = "1";
        String username = "tg_user";
        String messageText = "Hello";

        Message message = mock(Message.class);
        User user = mock(User.class);

        when(user.getId()).thenReturn(1L);
        when(user.getUserName()).thenReturn(username);
        when(message.getFrom()).thenReturn(user);
        when(message.hasText()).thenReturn(true);
        when(message.hasPhoto()).thenReturn(false);
        when(message.getText()).thenReturn(messageText);
        when(message.getMediaGroupId()).thenReturn(null);

        TelegramChat chat = TelegramChat
                .builder()
                .id(id)
                .chatId(chatId)
                .build();

        when(telegramChatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));

        SendMessage result = telegramSupportService.processSupportMessage(message);

        assertTrue(result.getText().contains(TelegramBotConstants.MESSAGE_SENT_TO_MANAGER_WAIT_FOR_RESPONSE));
        verify(telegramMessageRepository).save(any(TelegramMessage.class));
        verify(telegramNotificationService).notifyNewMessage(any(TelegramMessageDto.class), anyLong());
        verify(telegramNotificationService).notifyManagerAboutNewMessagesFromUser(username, messageText, id);
    }

    @Test
    void testProcessSupportMessage_HasPhotoMessageUploadingError_ShouldReturnSomethingWentWrongMessage() {
        long id = 1L;
        String chatId = "1";
        String username = "tg_user";

        Message message = mock(Message.class);
        User user = mock(User.class);

        when(user.getId()).thenReturn(1L);
        when(user.getUserName()).thenReturn(username);
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(1L);
        when(message.getMediaGroupId()).thenReturn(null);
        when(message.hasPhoto()).thenReturn(true);
        when(message.getPhoto()).thenReturn(List.of());

        TelegramChat chat = TelegramChat
                .builder()
                .id(id)
                .build();

        when(telegramChatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));

        SendMessage result = telegramSupportService.processSupportMessage(message);

        assertTrue(result.getText().contains(TelegramBotConstants.MANAGER_DIDNT_RECEIVED_YOUR_PHOTO_PLEASE_TRY_AGAIN));
        verify(telegramMessageRepository).save(any(TelegramMessage.class));
        verify(telegramNotificationService).notifyNewMessage(any(TelegramMessageDto.class), anyLong());
        verify(telegramNotificationService).notifyManagerAboutNewMessagesFromUser(username, TelegramBotConstants.PHOTO_CONTENT, id);
    }

//    @Test
//    void testProcessSupportMessage_HasOnePhotoMessageUploadingSuccess_ShouldReturnMessageSentToManagerMessage() {
//        long id = 1L;
//        String chatId = "1";
//        String username = "tg_user";
//
//        Message message = mock(Message.class);
//        User user = mock(User.class);
//        PhotoSize photoSize = mock(PhotoSize.class);
//        GetFile getFile = mock(GetFile.class);
//        File file = mock(File.class);
//        URI uri = mock(URI.class);
//
//        when(user.getId()).thenReturn(1L);
//        when(user.getUserName()).thenReturn(username);
//        when(message.getFrom()).thenReturn(user);
//        when(message.getChatId()).thenReturn(1L);
//        when(message.getMediaGroupId()).thenReturn(null);
//        when(message.hasPhoto()).thenReturn(true);
//        when(message.getPhoto()).thenReturn(List.of(photoSize));
//        when(photoSize.getFileSize()).thenReturn(10000);
//        when(photoSize.getFileId()).thenReturn("123456789");
//        when(file.getFilePath()).thenReturn("/path/to/file");
//        when(file.getFileUrl(anyString())).thenReturn("url");
//        when(URI.create(anyString())).thenReturn(uri);
//
//        TelegramChat chat = TelegramChat
//                .builder()
//                .id(id)
//                .build();
//
//        when(telegramChatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));
//        when(executor.executeGetFile(any(), any())).thenReturn(file);
//
//        SendMessage result = telegramSupportService.processSupportMessage(message);
//
//        assertTrue(result.getText().contains(TelegramBotConstants.MESSAGE_SENT_TO_MANAGER_WAIT_FOR_RESPONSE));
//        verify(telegramMessageRepository).save(any(TelegramMessage.class));
//        verify(telegramNotificationService).notifyNewMessage(any(TelegramMessageDto.class), anyLong());
//        verify(telegramNotificationService).notifyManagerAboutNewMessagesFromUser(username, TelegramBotConstants.PHOTO_CONTENT, id);
//        verify(fileService).upload(any(MultipartFile.class));
//    }
}
