package greencity.ubstelegrambot;

import greencity.constant.TelegramBotConstants;
import greencity.dto.telegram.TelegramMessageDto;
import greencity.entity.telegram.MessageAsset;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.telegram.TelegramMessage;
import greencity.enums.ChatState;
import greencity.exceptions.bots.TelegramBotExecutionException;
import greencity.producers.TelegramChatProducer;
import greencity.repository.MessageAssetRepository;
import greencity.repository.TelegramChatRepository;
import greencity.repository.TelegramMessageRepository;
import greencity.service.ubs.FileService;
import greencity.service.ubs.TelegramNotificationService;
import greencity.ubstelegrambot.messages.MessageFactory;
import greencity.ubstelegrambot.messages.MessageProvider;
import greencity.ubstelegrambot.service.TelegramExecutor;
import greencity.ubstelegrambot.service.TelegramSupportServiceImpl;
import greencity.ubstelegrambot.service.TelegramUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramSupportServiceTest {

    @InjectMocks
    private TelegramSupportServiceImpl telegramSupportService;

    @Mock
    private TelegramChatRepository telegramChatRepository;

    @Mock
    private TelegramNotificationService telegramNotificationService;

    @Mock
    private TelegramChatProducer telegramChatProducer;

    @Mock
    private TelegramMessageRepository telegramMessageRepository;

    @Mock
    private FileService fileService;

    @Mock
    private TelegramExecutor telegramExecutor;

    @Mock
    private MessageAssetRepository messageAssetRepository;

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
    void testProcessSupportMessage_UnknownChat_ShouldReturnUnknownErrorOccurredMessage() {
        Message message = mock(Message.class);
        User user = mock(User.class);

        when(message.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        when(telegramChatRepository.findByChatId(anyString())).thenReturn(Optional.empty());

        SendMessage result = telegramSupportService.processSupportMessage(message, TelegramBotConstants.UA);

        assertEquals(MessageProvider.get(TelegramBotConstants.UA, "unknown.error"),
            result.getText());
    }

    @Test
    void testProcessSupportMessage_EndSupportModeTextMessage_ShouldReturnStopSupportModeTextMessage() {
        try (MockedStatic<MessageFactory> mfMock = Mockito.mockStatic(MessageFactory.class)) {
            String chatId = "1";
            String lang = TelegramBotConstants.UA;
            String username = "tg_user";
            String endSupportText = MessageProvider.get(lang, "client.end.support.mode");

            Message message = mock(Message.class);
            User user = mock(User.class);
            when(user.getId()).thenReturn(1L);
            when(user.getUserName()).thenReturn(username);
            when(message.getFrom()).thenReturn(user);
            when(message.hasText()).thenReturn(true);
            when(message.getText()).thenReturn(endSupportText);

            TelegramChat chatEntity = TelegramChat.builder()
                .chatId(chatId)
                .languageCode(lang)
                .build();
            when(telegramChatRepository.findByChatId(chatId)).thenReturn(Optional.of(chatEntity));

            SendMessage feedbackMessage = new SendMessage(chatId, "feedback");
            SendMessage deleteKeyboardMessage = new SendMessage(chatId, "delete_keyboard");
            mfMock.when(() -> MessageFactory.createFeedbackMessage(chatId, lang))
                .thenReturn(feedbackMessage);
            mfMock.when(() -> MessageFactory.deleteEndSupportKeyboardMessage(chatId, lang))
                .thenReturn(deleteKeyboardMessage);

            when(telegramUtils.updateChatStateAndRespond(eq(chatId), eq(ChatState.NORMAL), eq(feedbackMessage)))
                .thenReturn(feedbackMessage);

            SendMessage result = telegramSupportService.processSupportMessage(message, lang);

            assertNotNull(result);
            assertEquals("feedback", result.getText());

            verify(telegramUtils).updateChatStateAndRespond(eq(chatId), eq(ChatState.NORMAL), eq(feedbackMessage));
            verify(telegramNotificationService).notifyManagerAboutEndSupportModeFromUser(username);
        }
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
            .unreadMessagesCount(0)
            .build();

        when(telegramChatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));

        SendMessage result = telegramSupportService.processSupportMessage(message, TelegramBotConstants.UA);

        assertTrue(result.getText().contains(MessageProvider.get(TelegramBotConstants.UA, "message.sent.to.manager")));
        verify(telegramMessageRepository).save(any(TelegramMessage.class));
        verify(telegramChatProducer).notifyNewMessage(any(TelegramMessageDto.class), anyLong());
        verify(telegramNotificationService).notifyManagerAboutNewMessagesFromUser(username, messageText, id);
    }

    @Test
    void testProcessSupportMessage_HasPhotoMessageHasMediaGroupExecuteGetFileError_ShouldReturnSomethingWentWrongMessage() {
        long id = 1L;
        String chatId = "1";
        String username = "tg_user";
        String mediaGroupId = "12345";

        Message message = mock(Message.class);
        User user = mock(User.class);
        PhotoSize photoSize = mock(PhotoSize.class);

        when(user.getId()).thenReturn(1L);
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(1L);
        when(message.getMediaGroupId()).thenReturn(mediaGroupId);
        when(message.hasPhoto()).thenReturn(true);
        when(message.getPhoto()).thenReturn(List.of(photoSize));
        when(photoSize.getFileId()).thenReturn("123456789");

        TelegramChat chat = TelegramChat
            .builder()
            .id(id)
            .unreadMessagesCount(0)
            .build();

        TelegramMessage telegramMessage = TelegramMessage
            .builder()
            .mediaGroupId(mediaGroupId)
            .build();

        when(telegramChatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));
        when(telegramMessageRepository.findByMediaGroupId(mediaGroupId)).thenReturn(Optional.of(telegramMessage));
        when(telegramExecutor.executeGetFile(any(GetFile.class))).thenReturn(null);

        SendMessage result = telegramSupportService.processSupportMessage(message, TelegramBotConstants.UA);

        assertTrue(result.getText().contains(MessageProvider.get(TelegramBotConstants.UA, "manager.photo.failed")));

        verify(telegramChatProducer, never()).notifyNewMessage(any(TelegramMessageDto.class), anyLong());
        verify(telegramNotificationService, never()).notifyManagerAboutNewMessagesFromUser(username,
            MessageProvider.get(TelegramBotConstants.UA, "photo.content"), id);
    }

    @Test
    void testProcessSupportMessage_HasPhotoMessageHasMediaGroupExecuteGetFileException_ShouldReturnSomethingWentWrongMessage() {
        long id = 1L;
        String chatId = "1";
        String username = "tg_user";
        String mediaGroupId = "12345";

        Message message = mock(Message.class);
        User user = mock(User.class);
        PhotoSize photoSize = mock(PhotoSize.class);

        when(user.getId()).thenReturn(1L);
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(1L);
        when(message.getMediaGroupId()).thenReturn(mediaGroupId);
        when(message.hasPhoto()).thenReturn(true);
        when(message.getPhoto()).thenReturn(List.of(photoSize));
        when(photoSize.getFileId()).thenReturn("123456789");

        TelegramChat chat = TelegramChat
            .builder()
            .id(id)
            .build();

        TelegramMessage telegramMessage = TelegramMessage
            .builder()
            .mediaGroupId(mediaGroupId)
            .build();

        when(telegramChatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));
        when(telegramMessageRepository.findByMediaGroupId(mediaGroupId)).thenReturn(Optional.of(telegramMessage));
        when(telegramExecutor.executeGetFile(any(GetFile.class))).thenThrow(new TelegramBotExecutionException());

        SendMessage result = telegramSupportService.processSupportMessage(message, TelegramBotConstants.UA);

        assertTrue(result.getText().contains(MessageProvider.get(TelegramBotConstants.UA, "manager.photo.failed")));
        verify(telegramChatProducer, never()).notifyNewMessage(any(TelegramMessageDto.class), anyLong());
        verify(telegramNotificationService, never()).notifyManagerAboutNewMessagesFromUser(username,
            MessageProvider.get(TelegramBotConstants.UA, "photo.content"), id);
    }

    @Test
    void testProcessSupportMessage_HasPhotoMessageHasMediaGroupNoTelegramFilePath_ShouldReturnSomethingWentWrongMessage() {
        long id = 1L;
        String chatId = "1";
        String username = "tg_user";
        String mediaGroupId = "12345";

        Message message = mock(Message.class);
        User user = mock(User.class);
        PhotoSize photoSize = mock(PhotoSize.class);
        File file = mock(File.class);

        when(user.getId()).thenReturn(1L);
        when(message.getFrom()).thenReturn(user);
        when(message.getMediaGroupId()).thenReturn(mediaGroupId);
        when(message.hasPhoto()).thenReturn(true);
        when(message.getPhoto()).thenReturn(List.of(photoSize));
        when(photoSize.getFileId()).thenReturn("123456789");
        when(file.getFilePath()).thenReturn(null);

        TelegramChat chat = TelegramChat
            .builder()
            .id(id)
            .unreadMessagesCount(0)
            .chatId(chatId)
            .build();

        TelegramMessage telegramMessage = TelegramMessage
            .builder()
            .mediaGroupId(mediaGroupId)
            .build();

        when(telegramChatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));
        when(telegramMessageRepository.findByMediaGroupId(mediaGroupId)).thenReturn(Optional.of(telegramMessage));
        when(telegramExecutor.executeGetFile(any(GetFile.class))).thenReturn(file);

        SendMessage result = telegramSupportService.processSupportMessage(message, TelegramBotConstants.UA);

        assertTrue(result.getText().contains(MessageProvider.get(TelegramBotConstants.UA, "manager.photo.failed")));

        verify(telegramChatProducer, never()).notifyNewMessage(any(TelegramMessageDto.class), anyLong());
        verify(telegramNotificationService, never()).notifyManagerAboutNewMessagesFromUser(username,
            MessageProvider.get(TelegramBotConstants.UA, "photo.content"), id);
    }

    @Test
    void testProcessSupportMessage_HasMoreThanOnePhotoMessageUploadingSuccess_ShouldReturnMessageSentToManagerMessage()
        throws IOException {
        long id = 1L;
        String chatId = "1";
        String mediaGroupId = "12345";

        Message message = mock(Message.class);
        User user = mock(User.class);
        PhotoSize photoSize = mock(PhotoSize.class);
        File file = mock(File.class);

        when(user.getId()).thenReturn(1L);
        when(message.getFrom()).thenReturn(user);
        when(message.getMediaGroupId()).thenReturn(mediaGroupId);
        when(message.hasPhoto()).thenReturn(true);
        when(message.getPhoto()).thenReturn(List.of(photoSize));
        when(photoSize.getFileSize()).thenReturn(10000);
        when(photoSize.getFileId()).thenReturn("123456789");
        when(file.getFilePath()).thenReturn("/path/to/file");

        TelegramChat chat = TelegramChat
            .builder()
            .id(id)
            .chatId(chatId)
            .build();

        TelegramMessage telegramMessage = TelegramMessage
            .builder()
            .mediaGroupId(mediaGroupId)
            .assets(new ArrayList<>())
            .build();

        when(telegramChatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));
        when(telegramMessageRepository.findByMediaGroupId(mediaGroupId)).thenReturn(Optional.of(telegramMessage));
        when(telegramExecutor.executeGetFile(any(GetFile.class))).thenReturn(file);
        when(telegramUtils.fileToByteArray(file)).thenReturn(new byte[] {1, 2, 3});
        when(fileService.upload(any(MultipartFile.class))).thenReturn("azureFileUrl");

        SendMessage result = telegramSupportService.processSupportMessage(message, TelegramBotConstants.UA);

        assertNull(result);
        verify(fileService).upload(any(MultipartFile.class));
        verify(messageAssetRepository).save(any(MessageAsset.class));
        verify(telegramMessageRepository).findByMediaGroupId(mediaGroupId);

        verify(telegramChatProducer, never()).notifyNewMessage(any(TelegramMessageDto.class), anyLong());
        verify(telegramNotificationService, never()).notifyManagerAboutNewMessagesFromUser(any(), any(),
            eq(chat.getId()));
    }

    @Test
    void testProcessSupportMessage_HasDocumentButNullDocument_ShouldReturnRetryMessage() {
        Message message = mock(Message.class);
        User user = mock(User.class);

        when(user.getId()).thenReturn(1L);
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(1L);
        when(message.hasText()).thenReturn(false);
        when(message.hasPhoto()).thenReturn(false);
        when(message.hasDocument()).thenReturn(true);
        when(message.getDocument()).thenReturn(null);

        TelegramChat chat = TelegramChat.builder()
            .id(1L)
            .chatId("1")
            .build();

        when(telegramChatRepository.findByChatId("1")).thenReturn(Optional.of(chat));

        SendMessage result = telegramSupportService.processSupportMessage(message, TelegramBotConstants.UA);

        assertEquals(MessageProvider.get(TelegramBotConstants.UA, "manager.file.failed"), result.getText());

        verify(telegramChatProducer, never()).notifyNewMessage(any(TelegramMessageDto.class), anyLong());
        verify(telegramNotificationService, never()).notifyManagerAboutNewMessagesFromUser(any(), any(),
            eq(chat.getId()));
    }

    @Test
    void testProcessSupportMessage_EmptyMessage_ShouldReturnRetryMessage() {
        Message message = mock(Message.class);
        User user = mock(User.class);
        String chatId = "1";

        when(user.getId()).thenReturn(1L);
        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(1L);
        when(message.hasText()).thenReturn(false);
        when(message.hasPhoto()).thenReturn(false);
        when(message.hasDocument()).thenReturn(false);

        TelegramChat chat = TelegramChat.builder()
            .id(1L)
            .chatId("1")
            .unreadMessagesCount(0)
            .build();

        when(telegramChatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));

        SendMessage result = telegramSupportService.processSupportMessage(message, TelegramBotConstants.UA);

        assertEquals(MessageProvider.get(TelegramBotConstants.UA, "manager.photo.failed"), result.getText());

        verify(telegramChatProducer, never()).notifyNewMessage(any(TelegramMessageDto.class), anyLong());
    }

    @Test
    void testProcessSupportMessage_HasDocumentMessage_ShouldUploadAndNotify() throws IOException {
        Message message = mock(Message.class);
        User user = mock(User.class);
        Document document = mock(Document.class);
        File file = mock(File.class);

        when(user.getId()).thenReturn(1L);
        when(message.getFrom()).thenReturn(user);
        when(message.hasDocument()).thenReturn(true);
        when(message.hasPhoto()).thenReturn(false);
        when(message.hasText()).thenReturn(false);
        when(message.getMediaGroupId()).thenReturn(null);
        when(message.getDocument()).thenReturn(document);
        when(document.getFileId()).thenReturn("fileId");
        when(document.getFileSize()).thenReturn(2048L);
        when(document.getMimeType()).thenReturn("application/pdf");
        when(document.getFileName()).thenReturn("file.pdf");

        when(telegramExecutor.executeGetFile(any(GetFile.class))).thenReturn(file);
        when(file.getFilePath()).thenReturn("/path/to/file.pdf");
        when(telegramUtils.fileToByteArray(file)).thenReturn(new byte[] {1, 2, 3});
        when(fileService.upload(any())).thenReturn("https://azure.com/file");

        TelegramChat chat = TelegramChat.builder()
            .id(1L)
            .chatId("1")
            .unreadMessagesCount(0)
            .build();

        when(telegramChatRepository.findByChatId("1")).thenReturn(Optional.of(chat));

        SendMessage result = telegramSupportService.processSupportMessage(message, TelegramBotConstants.UA);

        assertEquals(MessageProvider.get(TelegramBotConstants.UA, "message.sent.to.manager"), result.getText());
        verify(fileService).upload(any());
        verify(messageAssetRepository).save(any());

        verify(telegramMessageRepository).save(any(TelegramMessage.class));
        verify(telegramChatProducer).notifyNewMessage(any(TelegramMessageDto.class), anyLong());
        verify(telegramNotificationService).notifyManagerAboutNewMessagesFromUser(any(), any(), eq(chat.getId()));
    }

    @Test
    void testProcessSupportMessage_HasPhotoButNoLargestPhoto_ShouldDeleteMessageAndReturnRetryMessage() {
        String chatId = "1";
        long userId = 1L;

        Message message = mock(Message.class);
        User user = mock(User.class);
        TelegramChat chat = TelegramChat.builder()
            .chatId(chatId)
            .id(1L)
            .build();

        when(user.getId()).thenReturn(userId);
        when(message.getFrom()).thenReturn(user);
        when(message.hasText()).thenReturn(false);
        when(message.hasPhoto()).thenReturn(true);
        when(message.getMediaGroupId()).thenReturn(null);
        lenient().when(message.getPhoto()).thenReturn(Collections.emptyList());

        when(telegramChatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));

        ArgumentCaptor<TelegramMessage> messageCaptor = ArgumentCaptor.forClass(TelegramMessage.class);
        doNothing().when(telegramMessageRepository).delete(any());

        SendMessage result = telegramSupportService.processSupportMessage(message, TelegramBotConstants.UA);

        assertEquals(MessageProvider.get(TelegramBotConstants.UA, "manager.photo.failed"), result.getText());

        verify(telegramMessageRepository).save(messageCaptor.capture());
        verify(telegramMessageRepository).delete(messageCaptor.getValue());

        verify(telegramChatProducer, never()).notifyNewMessage(any(TelegramMessageDto.class), anyLong());
    }

    @Test
    void testProcessSupportMessage_HasDocumentWithNullMimeTypeAndFilename_ShouldUseUtilsToResolve() throws IOException {
        Message message = mock(Message.class);
        User user = mock(User.class);
        Document document = mock(Document.class);
        File file = mock(File.class);

        when(user.getId()).thenReturn(1L);
        when(message.getFrom()).thenReturn(user);
        when(message.hasDocument()).thenReturn(true);
        when(message.hasPhoto()).thenReturn(false);
        when(message.hasText()).thenReturn(false);
        when(message.getMediaGroupId()).thenReturn(null);

        when(message.getDocument()).thenReturn(document);
        when(document.getFileId()).thenReturn("fileId");
        when(document.getFileSize()).thenReturn(2048L);
        when(document.getMimeType()).thenReturn(null);
        when(document.getFileName()).thenReturn(null);

        when(telegramExecutor.executeGetFile(any(GetFile.class))).thenReturn(file);
        when(file.getFilePath()).thenReturn("/path/to/file.pdf");

        when(telegramUtils.fileToByteArray(file)).thenReturn(new byte[] {1, 2, 3});
        when(fileService.upload(any())).thenReturn("https://azure.com/file");

        TelegramChat chat = TelegramChat.builder()
            .id(1L)
            .chatId("1")
            .unreadMessagesCount(0)
            .build();

        when(telegramChatRepository.findByChatId("1")).thenReturn(Optional.of(chat));

        SendMessage result = telegramSupportService.processSupportMessage(message, TelegramBotConstants.UA);

        assertEquals(MessageProvider.get(TelegramBotConstants.UA, "message.sent.to.manager"), result.getText());

        verify(telegramMessageRepository).save(any(TelegramMessage.class));
        verify(telegramChatProducer).notifyNewMessage(any(TelegramMessageDto.class), anyLong());
        verify(telegramNotificationService).notifyManagerAboutNewMessagesFromUser(any(), any(), eq(chat.getId()));
        verify(fileService).upload(any(MultipartFile.class));
        verify(messageAssetRepository).save(any(MessageAsset.class));

    }

    @Test
    void testProcessSupportMessage_ImageWithCaption_ShouldCreateCorrectNotificationContent() throws Exception {
        long chatDbId = 1L;
        String chatId = "1";
        String username = "tg_user";
        String caption = "Hello, manager!";

        Message message = mock(Message.class);
        User user = mock(User.class);
        PhotoSize photoSize = mock(PhotoSize.class);
        File file = mock(File.class);

        when(user.getId()).thenReturn(1L);
        when(user.getUserName()).thenReturn(username);
        when(message.getFrom()).thenReturn(user);

        when(message.getMediaGroupId()).thenReturn(null);

        when(message.hasPhoto()).thenReturn(true);
        when(message.getPhoto()).thenReturn(List.of(photoSize));
        when(photoSize.getFileId()).thenReturn("file123");
        when(photoSize.getFileSize()).thenReturn(1_024);
        when(message.getText()).thenReturn(null);
        when(message.getCaption()).thenReturn(caption);

        TelegramChat chat = TelegramChat.builder()
            .id(chatDbId)
            .chatId(chatId)
            .build();
        when(telegramChatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));

        when(telegramExecutor.executeGetFile(any(GetFile.class))).thenReturn(file);
        when(file.getFilePath()).thenReturn("/path/photo.jpeg");
        when(telegramUtils.fileToByteArray(file)).thenReturn(new byte[] {1, 2, 3});
        when(fileService.upload(any(MultipartFile.class))).thenReturn("https://azure.com/photo");

        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        SendMessage result = telegramSupportService.processSupportMessage(message, TelegramBotConstants.UA);

        assertTrue(result.getText()
            .contains(MessageProvider.get(TelegramBotConstants.UA, "message.sent.to.manager")));

        verify(telegramNotificationService).notifyManagerAboutNewMessagesFromUser(
            eq(username),
            contentCaptor.capture(),
            eq(chatDbId));
        assertEquals("Image content (1 images) + text", contentCaptor.getValue());

        verify(fileService).upload(any(MultipartFile.class));
        verify(messageAssetRepository).save(any(MessageAsset.class));
    }
}
