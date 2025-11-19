package greencity.properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
class RemoteWebClientPropertiesTest {
    @Mock
    private Environment environment;

    @InjectMocks
    private RemoteWebClientProperties remoteWebClientProperties;

    private LogCaptor logCaptor;

    @BeforeEach
    void setUp() {
        logCaptor = LogCaptor.forClass(RemoteWebClientProperties.class);
        logCaptor.clearLogs();
    }

    @Test
    void getGreenCityUserAddress_shouldReturnValue() {
        when(environment.getProperty("greencity.redirect.user-server-address"))
            .thenReturn("https://greencity.com/user");

        String result = remoteWebClientProperties.getGreenCityUserAddress();

        assertEquals("https://greencity.com/user", result);
        verify(environment).getProperty("greencity.redirect.user-server-address");
    }

    @Test
    void getGreenCityUserAddress_shouldLogErrorIfEmpty() {
        when(environment.getProperty("greencity.redirect.user-server-address"))
            .thenReturn("");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> remoteWebClientProperties.getGreenCityUserAddress());

        assertEquals(ErrorMessage.USER_SERVER_ADDRESS_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.USER_SERVER_ADDRESS_NOT_FOUND));
    }

    @Test
    void getGreenCityUbsAddress_shouldReturnValue() {
        when(environment.getProperty("greencity.redirect.ubs-server-address"))
            .thenReturn("https://greencity.com/ubs");

        String result = remoteWebClientProperties.getGreenCityUbsAddress();

        assertEquals("https://greencity.com/ubs", result);
        verify(environment).getProperty("greencity.redirect.ubs-server-address");
    }

    @Test
    void getGreenCityUbsAddress_shouldLogErrorIfEmpty() {
        when(environment.getProperty("greencity.redirect.ubs-server-address"))
            .thenReturn("");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> remoteWebClientProperties.getGreenCityUbsAddress());

        assertEquals(ErrorMessage.UBS_SERVER_ADDRESS_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.UBS_SERVER_ADDRESS_NOT_FOUND));
    }

    @Test
    void getWebClientConnectTimeout_shouldReturnValue() {
        when(environment.getProperty("webclient.connection-timeout-millis", Integer.class))
            .thenReturn(5000);

        Integer result = remoteWebClientProperties.getWebClientConnectTimeout();

        assertEquals(5000, result);
        verify(environment).getProperty("webclient.connection-timeout-millis", Integer.class);
    }

    @Test
    void getWebClientConnectTimeout_shouldLogErrorIfNull() {
        when(environment.getProperty("webclient.connection-timeout-millis", Integer.class))
            .thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> remoteWebClientProperties.getWebClientConnectTimeout());

        assertEquals(ErrorMessage.WEB_CLIENT_CONNECTION_TIMEOUT_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.WEB_CLIENT_CONNECTION_TIMEOUT_NOT_FOUND));
    }

    @Test
    void getWebClientResponseTimeout_shouldReturnValue() {
        when(environment.getProperty("webclient.response-timeout-millis", Integer.class))
            .thenReturn(3000);

        Integer result = remoteWebClientProperties.getWebClientResponseTimeout();

        assertEquals(3000, result);
        verify(environment).getProperty("webclient.response-timeout-millis", Integer.class);
    }

    @Test
    void getWebClientResponseTimeout_shouldLogErrorIfNull() {
        when(environment.getProperty("webclient.response-timeout-millis", Integer.class))
            .thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> remoteWebClientProperties.getWebClientResponseTimeout());

        assertEquals(ErrorMessage.WEB_CLIENT_RESPONSE_TIMEOUT_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.WEB_CLIENT_RESPONSE_TIMEOUT_NOT_FOUND));
    }

    @Test
    void validateProperties_shouldLogInfo_whenAllPropertiesValid() {
        when(environment.getProperty("greencity.redirect.user-server-address"))
            .thenReturn("user-url");
        when(environment.getProperty("greencity.redirect.ubs-server-address"))
            .thenReturn("ubs-url");
        when(environment.getProperty("webclient.connection-timeout-millis", Integer.class))
            .thenReturn(2);
        when(environment.getProperty("webclient.response-timeout-millis", Integer.class))
            .thenReturn(5);

        remoteWebClientProperties.validateProperties();

        assertTrue(logCaptor.getInfoLogs()
            .contains("All Remote Web Client properties validated successfully."));
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void validateProperties_shouldThrowException_whenAnyPropertyInvalid() {
        when(environment.getProperty("greencity.redirect.user-server-address"))
            .thenReturn("user-url");
        when(environment.getProperty("greencity.redirect.ubs-server-address"))
            .thenReturn("ubs-url");
        when(environment.getProperty("webclient.connection-timeout-millis", Integer.class))
            .thenReturn(null);
        when(environment.getProperty("webclient.response-timeout-millis", Integer.class))
            .thenReturn(5);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> remoteWebClientProperties.validateProperties());

        assertEquals(ErrorMessage.WEB_CLIENT_CONNECTION_TIMEOUT_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.WEB_CLIENT_CONNECTION_TIMEOUT_NOT_FOUND));
    }
}