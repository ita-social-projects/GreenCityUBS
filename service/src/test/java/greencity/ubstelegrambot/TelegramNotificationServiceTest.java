package greencity.ubstelegrambot;

import greencity.entity.telegram.TelegramManager;
import greencity.repository.TelegramManagerRepository;
import greencity.service.ubs.TelegramLanguageService;
import greencity.ubstelegrambot.constant.TelegramConstants;
import greencity.ubstelegrambot.service.TelegramExecutor;
import greencity.ubstelegrambot.service.TelegramNotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramNotificationServiceTest {
    @InjectMocks
    private TelegramNotificationServiceImpl telegramNotificationService;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private UBSTelegramBot bot;

    @Mock
    private TelegramManagerRepository telegramManagerRepository;

    @Mock
    private TelegramExecutor executor;

    @Mock
    private TelegramLanguageService telegramLanguageService;

    @BeforeEach
    void setUp() {
        lenient().when(telegramLanguageService.getChatLanguage(anyString()))
            .thenReturn(TelegramConstants.UA);
    }

    @Test
    public void testNotifyManagerAboutNewMessagesFromUser_ManagersFound_MessageSent() {
        String username = "username";
        String messageText = "message";
        Long chatId = 123L;

        when(applicationContext.getBean(UBSTelegramBot.class)).thenReturn(bot);

        TelegramManager telegramManager1 = TelegramManager
            .builder()
            .chatId("123456789")
            .build();
        TelegramManager telegramManager2 = TelegramManager
            .builder()
            .chatId("123456711")
            .build();

        when(telegramManagerRepository.findAll()).thenReturn(List.of(telegramManager1, telegramManager2));

        telegramNotificationService.notifyManagerAboutNewMessagesFromUser(username, messageText, chatId);

        verify(executor, times(2)).executeCommand(eq(bot), any(SendMessage.class));
    }

    @Test
    public void testNotifyManagerAboutEndSupportModeFromUser_ManagersFound_MessageSent() {
        when(applicationContext.getBean(UBSTelegramBot.class)).thenReturn(bot);

        TelegramManager telegramManager1 = TelegramManager
                .builder()
                .chatId("123456789")
                .build();
        TelegramManager telegramManager2 = TelegramManager
                .builder()
                .chatId("123456711")
                .build();

        when(telegramManagerRepository.findAll()).thenReturn(List.of(telegramManager1, telegramManager2));

        telegramNotificationService.notifyManagerAboutEndSupportModeFromUser("username");

        verify(executor, times(2)).executeCommand(eq(bot), any(SendMessage.class));
    }
}
