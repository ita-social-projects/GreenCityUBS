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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthorizationPropertiesTest {
    @Mock
    private Environment environment;

    @InjectMocks
    private AuthorizationProperties authorizationProperties;

    private LogCaptor logCaptor;

    @BeforeEach
    void setUp() {
        logCaptor = LogCaptor.forClass(AuthorizationProperties.class);
        logCaptor.clearLogs();
    }

    @Test
    void getAccessTokenKey_shouldReturnValue_whenPropertyExists() {
        when(environment.getProperty("greencity.authorization.token-key"))
            .thenReturn("secret-key");

        String result = authorizationProperties.getAccessTokenKey();

        assertEquals("secret-key", result);
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void getAccessTokenKey_shouldLogError_whenPropertyMissing() {
        when(environment.getProperty("greencity.authorization.token-key"))
            .thenReturn("");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> authorizationProperties.getAccessTokenKey());

        assertEquals(ErrorMessage.JWT_SECRET_KEY_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.JWT_SECRET_KEY_NOT_FOUND));
    }

    @Test
    void getSystemEmailAddress_shouldReturnValue_whenPropertyExists() {
        when(environment.getProperty("greencity.authorization.service-email"))
            .thenReturn("system@greencity.com");

        String result = authorizationProperties.getSystemEmailAddress();

        assertEquals("system@greencity.com", result);
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void getSystemEmailAddress_shouldLogError_whenEmpty() {
        when(environment.getProperty("greencity.authorization.service-email"))
            .thenReturn("");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> authorizationProperties.getSystemEmailAddress());

        assertEquals(ErrorMessage.SYSTEM_EMAIL_ADDRES_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.SYSTEM_EMAIL_ADDRES_NOT_FOUND));
    }

    @Test
    void getSignInToken_shouldReturnValue_whenExists() {
        when(environment.getProperty("greencity.sing-in.secret-token"))
            .thenReturn("super-secret");

        String result = authorizationProperties.getSignInToken();

        assertEquals("super-secret", result);
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void getSignInToken_shouldLogError_whenEmpty() {
        when(environment.getProperty("greencity.sing-in.secret-token"))
            .thenReturn("");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> authorizationProperties.getSignInToken());

        assertEquals(ErrorMessage.SIGN_IN_TOKEN_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.SIGN_IN_TOKEN_NOT_FOUND));
    }

    @Test
    void validateProperties_shouldLogInfo_whenAllPropertiesValid() {
        when(environment.getProperty("greencity.authorization.token-key"))
            .thenReturn("key123");
        when(environment.getProperty("greencity.authorization.service-email"))
            .thenReturn("service@greencity.com");
        when(environment.getProperty("greencity.sing-in.secret-token"))
            .thenReturn("sign123");

        authorizationProperties.validateProperties();

        assertTrue(logCaptor.getInfoLogs()
            .contains("All authorization properties validated successfully."));
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void validateProperties_shouldThrowException_whenAnyPropertyInvalid() {
        when(environment.getProperty("greencity.authorization.token-key"))
            .thenReturn("key123");
        when(environment.getProperty("greencity.authorization.service-email"))
            .thenReturn("");
        when(environment.getProperty("greencity.sing-in.secret-token"))
            .thenReturn("sign123");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> authorizationProperties.validateProperties());

        assertEquals(ErrorMessage.SYSTEM_EMAIL_ADDRES_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.SYSTEM_EMAIL_ADDRES_NOT_FOUND));
    }
}