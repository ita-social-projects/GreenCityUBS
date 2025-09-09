package greencity.ubstelegrambot;

import greencity.constant.TelegramBotConstants;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.enums.AssetType;
import greencity.enums.ChatState;
import greencity.exceptions.NotFoundException;
import greencity.repository.PositionRepository;
import greencity.repository.TelegramChatRepository;
import greencity.ubstelegrambot.messages.MessageProvider;
import greencity.ubstelegrambot.service.TelegramUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramUtilsTest {
    @InjectMocks
    private TelegramUtils telegramUtils;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private TelegramChatRepository telegramChatRepository;

    @Mock
    private MultipartFile multipartFile;

    @Test
    void detectAssetTypeMultipartFile_WithImageContentType_ShouldReturnImage() {
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        AssetType result = TelegramUtils.detectAssetType(multipartFile);

        assertEquals(AssetType.IMAGE, result);
    }

    @Test
    void detectAssetTypeMultipartFile_WithAudioContentType_ShouldReturnAudio() {
        when(multipartFile.getContentType()).thenReturn("audio/mpeg");
        AssetType result = TelegramUtils.detectAssetType(multipartFile);

        assertEquals(AssetType.AUDIO, result);
    }

    @Test
    void detectAssetTypeMultipartFile_WithVideoContentType_ShouldReturnVideo() {
        when(multipartFile.getContentType()).thenReturn("video/mp4");
        AssetType result = TelegramUtils.detectAssetType(multipartFile);

        assertEquals(AssetType.VIDEO, result);
    }

    @Test
    void detectAssetTypeMultipartFile_WithUnknownContentType_ShouldReturnFile() {
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        AssetType result = TelegramUtils.detectAssetType(multipartFile);

        assertEquals(AssetType.FILE, result);
    }

//    @Test
//    void detectAssetTypeMultipartFile_WithNull_ShouldReturnFile() {
//        when(multipartFile.getContentType()).thenReturn(null);
//        AssetType result = TelegramUtils.detectAssetType(multipartFile);
//
//        assertEquals(AssetType.FILE, result);
//    }

    @Test
    void detectAssetType_WithImageContentType_ShouldReturnImage() {
        assertEquals(AssetType.IMAGE, TelegramUtils.detectAssetType("image/jpeg"));
    }

    @Test
    void detectAssetType_WithVideoContentType_ShouldReturnVideo() {
        assertEquals(AssetType.VIDEO, TelegramUtils.detectAssetType("video/mp4"));
    }

    @Test
    void detectAssetType_WithAudioContentType_ShouldReturnAudio() {
        assertEquals(AssetType.AUDIO, TelegramUtils.detectAssetType("audio/mpeg"));
    }

    @Test
    void detectAssetType_WithUnknownContentType_ShouldReturnFile() {
        assertEquals(AssetType.FILE, TelegramUtils.detectAssetType("application/pdf"));
    }

//    @Test
//    void detectAssetType_WithNull_ShouldReturnFile() {
//        assertEquals(AssetType.FILE, TelegramUtils.detectAssetType((String) null));
//    }

    @Test
    void getFileNameFromPath_WithSlash_ShouldReturnFileName() {
        String path = "application/user/image.jpeg";
        assertEquals("image.jpeg", TelegramUtils.getFileNameFromPath(path));
    }

    @Test
    void getFileNameFromPath_WithoutSlash_ShouldReturnFileName() {
        String path = "image.jpeg";
        assertEquals("image.jpeg", TelegramUtils.getFileNameFromPath(path));
    }

    @Test
    void getFileNameFromPath_WithNull_ShouldReturnNull() {
        assertNull(TelegramUtils.getFileNameFromPath(null));
    }

    @Test
    void getFileNameFromPath_WithEmptyFilePath_ShouldReturnNull() {
        assertNull(TelegramUtils.getFileNameFromPath(""));
    }

    @Test
    void getFileContentType_ShouldReturnImageTypes() {
        assertEquals("image/jpeg", TelegramUtils.getFileContentType("image.jpeg"));
        assertEquals("image/jpeg", TelegramUtils.getFileContentType("image.jpg"));
        assertEquals("image/png", TelegramUtils.getFileContentType("image.png"));
        assertEquals("image/gif", TelegramUtils.getFileContentType("image.gif"));
    }

    @Test
    void getFileContentType_WithNull_ShouldReturnDefault() {
        assertEquals("application/octet-stream", TelegramUtils.getFileContentType(null));
    }

    @Test
    void getFileContentType_withExtension_ShouldReturnDefault() {
        assertEquals("application/octet-stream", TelegramUtils.getFileContentType("document.docx"));
    }

    @Test
    void isValidEmail_WithValidEmail_ShouldReturnTrue() {
        assertTrue(TelegramUtils.isValidEmail("anastasia2001@gmail.com"));
    }

    @Test
    void isValidEmail_WithInvalidEmail_ShouldReturnFalse() {
        assertFalse(TelegramUtils.isValidEmail("anastasia2001"));
    }

    @Test
    void isValidEmail_WithNull_ShouldReturnFalse() {
        assertFalse(TelegramUtils.isValidEmail(null));
    }

    @Test
    void checkIsEmployeeManager_WhenManagerPositionPresent_ShouldReturnTrue() {
        Position manager = new Position();
        manager.setId(2L);

        Employee employee = new Employee();
        employee.setEmployeePosition(Set.of(manager));

        when(positionRepository.findById(1L)).thenReturn(Optional.of(new Position()));
        when(positionRepository.findById(2L)).thenReturn(Optional.of(manager));

        boolean result = telegramUtils.checkIsEmployeeManager(employee);
        assertTrue(result);
    }

    @Test
    void checkIsEmployeeManager_WhenServiceManagerPositionPresent_ShouldReturnTrue() {
        Position serviceManager = new Position();
        serviceManager.setId(1L);

        Employee employee = new Employee();
        employee.setEmployeePosition(Set.of(serviceManager));

        when(positionRepository.findById(1L)).thenReturn(Optional.of(serviceManager));
        when(positionRepository.findById(2L)).thenReturn(Optional.of(new Position()));

        boolean result = telegramUtils.checkIsEmployeeManager(employee);
        assertTrue(result);
    }

    @Test
    void checkIsEmployeeManager_WhenPositionNotFound_ShouldThrowNotFoundException() {
        Employee employee = new Employee();
        employee.setEmployeePosition(Set.of());

        when(positionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> telegramUtils.checkIsEmployeeManager(employee));
    }

    @Test
    void updateChatStateAndRespond_ShouldUpdateStateAndReturnMessage() {
        TelegramChat chat = new TelegramChat();
        chat.setChatId("123");
        chat.setChatState(ChatState.NORMAL);

        when(telegramChatRepository.findByChatId("123")).thenReturn(Optional.of(chat));
        when(telegramChatRepository.save(any())).thenReturn(chat);

        SendMessage message = new SendMessage();
        message.setText("Updated chat state");

        SendMessage result = telegramUtils.updateChatStateAndRespond(
            "123",
            ChatState.MAKING_FEEDBACK,
            message);

        assertEquals(message, result);
        assertEquals(ChatState.MAKING_FEEDBACK, chat.getChatState());
    }

    @Test
    void updateChatStateAndRespond_WhenChatNotFound_ShouldReturnErrorMessage() {
        when(telegramChatRepository.findByChatId("999")).thenReturn(Optional.empty());
        String id = "1";
        SendMessage result = telegramUtils.updateChatStateAndRespond(
                "999",
                ChatState.NORMAL,
                new SendMessage(id, "Should not be called")
        );

        assertEquals("999", result.getChatId());
        assertTrue(result.getText().contains(MessageProvider.get(TelegramBotConstants.UK,"unknown.error")));
    }
}
