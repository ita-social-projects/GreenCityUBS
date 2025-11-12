package greencity.properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import greencity.constant.ErrorMessage;
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

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> googleProperties.getGoogleApiKey());

        assertEquals(ErrorMessage.GOOGLE_API_KEY_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.GOOGLE_API_KEY_NOT_FOUND));
    }

    @Test
    void getGoogleApiKey_shouldLogError_whenNull() {
        when(environment.getProperty("greencity.authorization.googleApiKey"))
            .thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> googleProperties.getGoogleApiKey());

        assertEquals(ErrorMessage.GOOGLE_API_KEY_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.GOOGLE_API_KEY_NOT_FOUND));
    }

    @Test
    void validateProperties_shouldLogInfo_whenAllPropertiesValid() {
        when(environment.getProperty("greencity.authorization.googleApiKey"))
            .thenReturn("key123");

        googleProperties.validateProperties();

        assertTrue(logCaptor.getInfoLogs()
            .contains("All Google properties validated successfully."));
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void validateProperties_shouldThrowException_whenAnyPropertyInvalid() {
        when(environment.getProperty("greencity.authorization.googleApiKey"))
            .thenReturn("");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> googleProperties.validateProperties());

        assertEquals(ErrorMessage.GOOGLE_API_KEY_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.GOOGLE_API_KEY_NOT_FOUND));
    }
}