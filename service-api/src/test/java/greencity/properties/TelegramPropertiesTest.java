package greencity.properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
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

        String result = telegramProperties.getTelegramBotName();

        assertEquals("", result);
        assertTrue(logCaptor.getErrorLogs().contains("The greencity bots name is empty"));
    }

    @Test
    void getTelegramBotName_shouldLogError_whenPropertyNull() {
        when(environment.getProperty("greencity.bots.ubs-bot-name"))
            .thenReturn(null);

        String result = telegramProperties.getTelegramBotName();

        assertNull(result);
        assertTrue(logCaptor.getErrorLogs().contains("The greencity bots name is empty"));
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

        String result = telegramProperties.getTelegramBotToken();

        assertEquals("", result);
        assertTrue(logCaptor.getErrorLogs().contains("The greencity bots token is empty"));
    }

    @Test
    void getTelegramBotToken_shouldLogError_whenPropertyNull() {
        when(environment.getProperty("greencity.bots.ubs-bot-token"))
            .thenReturn(null);

        String result = telegramProperties.getTelegramBotToken();

        assertNull(result);
        assertTrue(logCaptor.getErrorLogs().contains("The greencity bots token is empty"));
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

        String result = telegramProperties.getUbsAdminBaseUrl();

        assertEquals("", result);
        assertTrue(logCaptor.getErrorLogs().contains("The greencity admin base url is empty"));
    }

    @Test
    void getUbsAdminBaseUrl_shouldLogError_whenPropertyNull() {
        when(environment.getProperty("greencity.bots.ubs-bot-ui"))
            .thenReturn(null);

        String result = telegramProperties.getUbsAdminBaseUrl();

        assertNull(result);
        assertTrue(logCaptor.getErrorLogs().contains("The greencity admin base url is empty"));
    }
}