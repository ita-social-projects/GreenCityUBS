package greencity.ubstelegrambot;

import greencity.constant.TelegramBotConstants;
import greencity.entity.telegram.TelegramManager;
import greencity.enums.MessageType;
import greencity.properties.TelegramProperties;
import greencity.repository.TelegramManagerRepository;
import greencity.service.ubs.TelegramLanguageService;
import greencity.ubstelegrambot.service.TelegramBotResponseServiceImpl;
import greencity.ubstelegrambot.service.TelegramExecutor;
import greencity.ubstelegrambot.service.TelegramNotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramNotificationServiceTest {
    @InjectMocks
    private TelegramNotificationServiceImpl telegramNotificationService;

    @Mock
    private TelegramManagerRepository telegramManagerRepository;

    @Mock
    private TelegramExecutor telegramExecutor;

    @Mock
    private TelegramLanguageService telegramLanguageService;

    @Mock
    private TelegramBotResponseServiceImpl telegramBotResponseService;
    @Mock
    private TelegramProperties telegramProperties;

    private static final String BASE_URL = "http://localhost:8080/";

    @BeforeEach
    void setUp() {
        lenient().when(telegramLanguageService.getChatLanguage(anyString()))
            .thenReturn(TelegramBotConstants.UK);
    }

    @Test
    void testNotifyManagerAboutNewMessagesFromUser_ManagersFound_MessageSent() {
        String username = "username";
        String messageText = "message";
        Long chatId = 123L;

        TelegramManager telegramManager1 = TelegramManager
            .builder()
            .chatId("123456789")
            .build();
        TelegramManager telegramManager2 = TelegramManager
            .builder()
            .chatId("123456711")
            .build();

        when(telegramProperties.getUbsAdminBaseUrl()).thenReturn("baseUrl");
        when(telegramManagerRepository.findAll()).thenReturn(List.of(telegramManager1, telegramManager2));
        when(telegramBotResponseService.getResponseByLangAndMessageType(anyString(),
            eq(MessageType.CLIENT_WANT_TO_SPEAK))).thenReturn("text");

        telegramNotificationService.notifyManagerAboutNewMessagesFromUser(username, messageText, chatId);

        verify(telegramExecutor, times(2)).executeCommand(any(SendMessage.class));
    }

    @Test
    void testNotifyManagerAboutEndSupportModeFromUser_ManagersFound_MessageSent() {
        TelegramManager telegramManager1 = TelegramManager
            .builder()
            .chatId("123456789")
            .build();
        TelegramManager telegramManager2 = TelegramManager
            .builder()
            .chatId("123456711")
            .build();

        when(telegramManagerRepository.findAll()).thenReturn(List.of(telegramManager1, telegramManager2));
        when(telegramBotResponseService.getResponseByLangAndMessageType(anyString(),
            eq(MessageType.CLIENT_END_SUPPORT_NOTIFICATION))).thenReturn("text");

        telegramNotificationService.notifyManagerAboutEndSupportModeFromUser("username");

        verify(telegramExecutor, times(2)).executeCommand(any(SendMessage.class));
    }
}
