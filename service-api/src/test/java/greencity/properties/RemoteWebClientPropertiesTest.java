package greencity.properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
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

        String result = remoteWebClientProperties.getGreenCityUserAddress();

        assertEquals("", result);
        assertTrue(logCaptor.getErrorLogs().contains("The redirect user server address is empty"));
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

        Integer result = remoteWebClientProperties.getWebClientConnectTimeout();

        assertNull(result);
        assertTrue(logCaptor.getErrorLogs().contains("The webclient connection timeout is empty"));
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

        Integer result = remoteWebClientProperties.getWebClientResponseTimeout();

        assertNull(result);
        assertTrue(logCaptor.getErrorLogs().contains("The webclient response timeout is empty"));
    }
}