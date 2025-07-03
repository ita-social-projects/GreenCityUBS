package greencity.ubstelegrambot.service;

//import greencity.mapping.telegrammessage.ImageConverter;
import greencity.repository.TelegramChatRepository;
//import greencity.repository.TelegramImageRepository;
import greencity.service.ubs.AzureCloudStorageService;
import greencity.service.ubs.TelegramService;
import greencity.service.ubs.TelegramStreamingService;
import greencity.ubstelegrambot.UBSTelegramBot;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
public class TelegramPhotoServiceImplTest {

    @Mock
    private UBSTelegramBot ubsTelegramBot;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private TelegramStreamingService telegramStreamingService;

    @Mock
    private AzureCloudStorageService azureCloudStorageService;

//    @Mock
//    private TelegramImageRepository userPhotosRepository;

    @Mock
    private TelegramExecutor telegramExecutor;

    @Mock
    private TelegramChatRepository telegramChatRepository;

    @Mock
    private TelegramService telegramService;

    @Mock
    private ModelMapper modelMapper;

//    @Mock
//    private ImageConverter imageConverter;

//    @InjectMocks
//    private TelegramPhotoServiceImpl telegramPhotoService;

//    @Test
//    void downloadPhotoFromTelegramTestWithValidMessage() {
//        String fileId = "fileId";
//        String botToken = "botToken";
//        String fileUrl = "fileUrl";
//        PhotoSize photoSize = new PhotoSize();
//        photoSize.setFileId(fileId);
//
//        Message message = new Message();
//        message.setPhoto(List.of(
//            photoSize));
//
//        File file = spy(new File());
//
//        lenient().when(telegramExecutor.executeGetFile(eq(ubsTelegramBot), any(GetFile.class)))
//            .thenReturn(file);
//        when(ubsTelegramBot.getBotToken())
//            .thenReturn(botToken);
//        when(applicationContext.getBean(UBSTelegramBot.class)).thenReturn(ubsTelegramBot);
//        when(file.getFileUrl(botToken))
//            .thenReturn(fileUrl);
//
//        List<String> actualResult = telegramPhotoService.downloadPhotoFromTelegram(
//            message);
//
//        assertEquals(
//            actualResult,
//            List.of(
//                fileUrl));
//        verify(telegramExecutor).executeGetFile(eq(ubsTelegramBot), any(GetFile.class));
//        verify(ubsTelegramBot).getBotToken();
//    }

//    @Test
//    void downloadPhotoFromTelegramTestWithNoPhotoInMessage() {
//        Message message = new Message();
//        message.setPhoto(Collections.emptyList());
//
//        assertThrows(
//            BadRequestException.class,
//            () -> telegramPhotoService.downloadPhotoFromTelegram(
//                message));
//        verify(telegramExecutor, never()).executeGetFile(any(), any());
//    }
//
//    @Test
//    void savePhotoToAzureBlobWithListOfUrlsTest() {
//        String mockUrl = "url";
//        List<String> photoUrls = List.of(
//            "https://www.argospetinsurance.co.uk/assets/uploads/2017/12/cat-pet-animal-domestic-104827.jpeg",
//            "https://www.argospetinsurance.co.uk/assets/uploads/2017/12/cat-pet-animal-domestic-104827.jpeg");
//        List<String> expectedResult = List.of(
//            mockUrl,
//            mockUrl);
//
//        when(azureCloudStorageService.upload(any(MultipartFile.class)))
//            .thenReturn(mockUrl);
//
//        List<String> actualResult = telegramPhotoService.savePhotoToAzureBlob(photoUrls);
//
//        assertEquals(expectedResult, actualResult);
//        verify(azureCloudStorageService, times(photoUrls.size()))
//            .upload(any(MultipartFile.class));
//    }

//    @Test
//    void savePhotoToAzureBlobWithListOfUrlsTestThrowsExceptionOnInvalidUrl() {
//        List<String> photoUrls = List.of(
//            "fakeUrl1",
//            "fakeUrl2");
//
//        assertThrows(
//            FileNotSavedException.class,
//            () -> telegramPhotoService.savePhotoToAzureBlob(photoUrls));
//        verify(azureCloudStorageService, never()).upload(any());
//    }
//
//    @Test
//    void savePhotoToAzureBlobTest() {
//        MultipartFile multipartFile = new MockMultipartFile(
//            "name",
//            "text.txt",
//            "text/plain",
//            "content".getBytes());
//        String expectedResult = "url";
//
//        when(azureCloudStorageService.upload(multipartFile))
//            .thenReturn(expectedResult);
//
//        String actualResult = telegramPhotoService
//            .savePhotoToAzureBlob(multipartFile);
//
//        assertEquals(expectedResult, actualResult);
//        verify(azureCloudStorageService).upload(multipartFile);
//    }

//    @Test
//    void deletePhotoFromAzureBlobTest() {
//        String url = "https://www.argospetinsurance.co.uk/assets/uploads/2017/12/cat-pet-animal-domestic-104827.jpeg";
//
//        telegramPhotoService.deletePhotoFromAzureBlob(url);
//
//        verify(azureCloudStorageService).delete(url);
//    }
//
//    @Test
//    void saveToDBTest() {
//        List<String> photoUrls = List.of(
//            "url1",
//            "url2",
//            "url3");
//        int amountOfUrls = photoUrls.size();
//        String chatId = "123";
//        String caption = "caption";
//
//        //telegramPhotoService.saveToDB(photoUrls, chatId, caption);
//
//        verify(userPhotosRepository, times(amountOfUrls)).save(any(Image.class));
//    }

//    @Test
//    void sendPhotoToUserTest() {
//        String chatId = "123";
//        String photoUrl = "url";
//        String caption = "caption";
//        TelegramChat telegramChat = new TelegramChat();
//        SendPhoto sendPhoto = new SendPhoto();
//        Message message = new Message();
//        PhotoSize largestPhotoSize = new PhotoSize(
//            "fileId",
//            "fileUniqueId",
//            1,
//            1,
//            1,
//            "filePath");
//        message.setPhoto(List.of(
//            largestPhotoSize));
//        File file = new File();
//        String fileUrl = "fileUrl";
//        List<String> photoUrls = List.of(fileUrl);
//        String botToken = "botToken";
//
//        try (MockedStatic<MessageFactory> mockedStatic = Mockito.mockStatic(MessageFactory.class)) {
//            when(telegramChatRepository.findByChatId(chatId))
//                .thenReturn(Optional.of(telegramChat));
//            when(applicationContext.getBean(UBSTelegramBot.class))
//                .thenReturn(ubsTelegramBot);
//            mockedStatic.when(() -> MessageFactory.createPhotoSender(chatId, photoUrl, caption))
//                .thenReturn(sendPhoto);
//            when(telegramExecutor.executeSendPhoto(ubsTelegramBot, sendPhoto))
//                .thenReturn(message);
//            when(telegramExecutor.executeGetFile(eq(ubsTelegramBot), any()))
//                .thenReturn(file);
//            when(ubsTelegramBot.getBotToken())
//                .thenReturn(botToken);
//            when(file.getFileUrl(ubsTelegramBot.getBotToken()))
//                .thenReturn(fileUrl);
//
//            telegramPhotoService.sendPhotoToUser(
//                chatId,
//                photoUrl,
//                caption);
//
//            verify(telegramChatRepository).findByChatId(chatId);
//            verify(applicationContext, times(2)).getBean(UBSTelegramBot.class);
//            verify(telegramExecutor).executeSendPhoto(ubsTelegramBot, sendPhoto);
//            verify(telegramExecutor).executeGetFile(eq(ubsTelegramBot), any(GetFile.class));
//            verify(ubsTelegramBot).getBotToken();
//            verify(userPhotosRepository, times(photoUrls.size())).save(any(Image.class));
//        }
//    }

//    @Test
//    void sendPhotoToUserTestThrowsBadRequestExceptionOnMessageWithoutPhoto() {
//        String chatId = "123";
//        String photoUrl = "url";
//        String caption = "caption";
//        TelegramChat telegramChat = new TelegramChat();
//        SendPhoto emptySendPhoto = new SendPhoto();
//        Message emptyMessage = new Message();
//        emptyMessage.setPhoto(Collections.emptyList());
//
//        try (MockedStatic<MessageFactory> mockedStatic = Mockito.mockStatic(MessageFactory.class)) {
//            when(telegramChatRepository.findByChatId(chatId))
//                .thenReturn(Optional.of(telegramChat));
//            when(applicationContext.getBean(UBSTelegramBot.class))
//                .thenReturn(ubsTelegramBot);
//            mockedStatic.when(() -> MessageFactory.createPhotoSender(chatId, photoUrl, caption))
//                .thenReturn(emptySendPhoto);
//            when(telegramExecutor.executeSendPhoto(ubsTelegramBot, emptySendPhoto))
//                .thenReturn(emptyMessage);
//
//            assertThrows(
//                BadRequestException.class,
//                () -> telegramPhotoService.sendPhotoToUser(
//                    chatId,
//                    photoUrl,
//                    caption));
//            verify(telegramChatRepository).findByChatId(chatId);
//            verify(applicationContext, times(2)).getBean(UBSTelegramBot.class);
//            verify(telegramExecutor).executeSendPhoto(ubsTelegramBot, emptySendPhoto);
//        }
//    }

//    @Test
//    void sendPhotoToUserTestThrowsNotFoundExceptionOnChatNotFound() {
//        String chatId = "123";
//        String photoUrl = "url";
//        String caption = "caption";
//
//        when(telegramChatRepository.findByChatId(chatId))
//            .thenReturn(Optional.empty());
//
//        assertThrows(
//            NotFoundException.class,
//            () -> telegramPhotoService.sendPhotoToUser(
//                chatId,
//                photoUrl,
//                caption));
//        verify(telegramChatRepository).findByChatId(chatId);
//    }
}
