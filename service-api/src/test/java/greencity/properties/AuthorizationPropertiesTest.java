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

        String result = authorizationProperties.getAccessTokenKey();

        assertEquals("", result);
        assertTrue(logCaptor.getErrorLogs().contains("Authorization token key not set"));
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

        String result = authorizationProperties.getSystemEmailAddress();

        assertEquals("", result);
        assertTrue(logCaptor.getErrorLogs().contains("SystemEmailAddress property is empty"));
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

        String result = authorizationProperties.getSignInToken();

        assertEquals("", result);
        assertTrue(logCaptor.getErrorLogs().contains("SingInToken property is empty"));
    }
}