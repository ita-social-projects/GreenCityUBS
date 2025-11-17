package greencity.properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import greencity.constant.ErrorMessage;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TelegramPropertiesTest {

    @Mock
    private Environment environment;

    @InjectMocks
    private TelegramProperties telegramProperties;

    private LogCaptor logCaptor;

    @BeforeEach
    void setUp() {
        logCaptor = LogCaptor.forClass(TelegramProperties.class);
        logCaptor.clearLogs();
    }

    @Test
    void getTelegramBotName_shouldReturnValue_whenPropertyExists() {
        when(environment.getProperty("greencity.bots.ubs-bot-name"))
            .thenReturn("TestBot");

        String result = telegramProperties.getTelegramBotName();

        assertEquals("TestBot", result);
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void getTelegramBotName_shouldLogError_whenPropertyEmpty() {
        when(environment.getProperty("greencity.bots.ubs-bot-name"))
            .thenReturn("");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> telegramProperties.getTelegramBotName());

        assertEquals(ErrorMessage.TELEGRAM_BOT_NAME_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.TELEGRAM_BOT_NAME_NOT_FOUND));
    }

    @Test
    void getTelegramBotName_shouldLogError_whenPropertyNull() {
        when(environment.getProperty("greencity.bots.ubs-bot-name"))
            .thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> telegramProperties.getTelegramBotName());

        assertEquals(ErrorMessage.TELEGRAM_BOT_NAME_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.TELEGRAM_BOT_NAME_NOT_FOUND));
    }

    @Test
    void getTelegramBotToken_shouldReturnValue_whenPropertyExists() {
        when(environment.getProperty("greencity.bots.ubs-bot-token"))
            .thenReturn("TestToken");

        String result = telegramProperties.getTelegramBotToken();

        assertEquals("TestToken", result);
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void getTelegramBotToken_shouldLogError_whenPropertyEmpty() {
        when(environment.getProperty("greencity.bots.ubs-bot-token"))
            .thenReturn("");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> telegramProperties.getTelegramBotToken());

        assertEquals(ErrorMessage.TELEGRAM_BOT_TOKEN_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.TELEGRAM_BOT_TOKEN_NOT_FOUND));
    }

    @Test
    void getTelegramBotToken_shouldLogError_whenPropertyNull() {
        when(environment.getProperty("greencity.bots.ubs-bot-token"))
            .thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> telegramProperties.getTelegramBotToken());

        assertEquals(ErrorMessage.TELEGRAM_BOT_TOKEN_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.TELEGRAM_BOT_TOKEN_NOT_FOUND));
    }

    @Test
    void getUbsAdminBaseUrl_shouldReturnValue_whenPropertyExists() {
        when(environment.getProperty("greencity.bots.ubs-bot-ui"))
            .thenReturn("http://test.url");

        String result = telegramProperties.getUbsAdminBaseUrl();

        assertEquals("http://test.url", result);
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void getUbsAdminBaseUrl_shouldLogError_whenPropertyEmpty() {
        when(environment.getProperty("greencity.bots.ubs-bot-ui"))
            .thenReturn("");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> telegramProperties.getUbsAdminBaseUrl());

        assertEquals(ErrorMessage.GREENCITY_ADMIN_BASE_URL_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.GREENCITY_ADMIN_BASE_URL_NOT_FOUND));
    }

    @Test
    void getUbsAdminBaseUrl_shouldLogError_whenPropertyNull() {
        when(environment.getProperty("greencity.bots.ubs-bot-ui"))
            .thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> telegramProperties.getUbsAdminBaseUrl());

        assertEquals(ErrorMessage.GREENCITY_ADMIN_BASE_URL_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.GREENCITY_ADMIN_BASE_URL_NOT_FOUND));
    }

    @Test
    void validateProperties_shouldLogInfo_whenAllPropertiesValid() {
        when(environment.getProperty("greencity.bots.ubs-bot-name"))
            .thenReturn("name");
        when(environment.getProperty("greencity.bots.ubs-bot-token"))
            .thenReturn("token");
        when(environment.getProperty("greencity.bots.ubs-bot-ui"))
            .thenReturn("url");

        telegramProperties.validateProperties();

        assertTrue(logCaptor.getInfoLogs()
            .contains("All Telegram properties validated successfully."));
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void validateProperties_shouldThrowException_whenAnyPropertyInvalid() {
        when(environment.getProperty("greencity.bots.ubs-bot-name"))
            .thenReturn("name");
        when(environment.getProperty("greencity.bots.ubs-bot-token"))
            .thenReturn("");
        when(environment.getProperty("greencity.bots.ubs-bot-ui"))
            .thenReturn("url");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> telegramProperties.validateProperties());

        assertEquals(ErrorMessage.TELEGRAM_BOT_TOKEN_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.TELEGRAM_BOT_TOKEN_NOT_FOUND));
    }
}