package greencity.properties;

import static org.junit.jupiter.api.Assertions.*;
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
class GooglePropertiesTest {
    @Mock
    private Environment environment;

    @InjectMocks
    private GoogleProperties googleProperties;

    private LogCaptor logCaptor;

    @BeforeEach
    void setUp() {
        logCaptor = LogCaptor.forClass(GoogleProperties.class);
        logCaptor.clearLogs();
    }

    @Test
    void getGoogleApiKey_shouldReturnValue_whenPropertyExists() {
        when(environment.getProperty("greencity.authorization.googleApiKey"))
            .thenReturn("fake-google-api-key");

        String result = googleProperties.getGoogleApiKey();

        assertEquals("fake-google-api-key", result);
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void getGoogleApiKey_shouldLogError_whenEmpty() {
        when(environment.getProperty("greencity.authorization.googleApiKey"))
            .thenReturn("");

        String result = googleProperties.getGoogleApiKey();

        assertEquals("", result);
        assertTrue(logCaptor.getErrorLogs().contains("Google API Key not set"));
    }

    @Test
    void getGoogleApiKey_shouldLogError_whenNull() {
        when(environment.getProperty("greencity.authorization.googleApiKey"))
            .thenReturn(null);

        String result = googleProperties.getGoogleApiKey();

        assertNull(result);
        assertTrue(logCaptor.getErrorLogs().contains("Google API Key not set"));
    }
}