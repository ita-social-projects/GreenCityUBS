package greencity.security;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PreAuthorizerTest {
    private final PreAuthorizer preAuthorizer = new PreAuthorizer();

    @Test
    void testHasAuthorityWithValidAuthorityShouldReturnTrue() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getCredentials()).thenReturn(List.of("ROLE_USER", "ROLE_ADMIN"));
        when(authentication.getPrincipal()).thenReturn("testUser");

        boolean result = preAuthorizer.hasAuthority("ROLE_ADMIN", authentication);

        assertTrue(result);
    }

    @Test
    void testHasAuthorityWithInvalidAuthorityShouldReturnFalse() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getCredentials()).thenReturn(List.of("ROLE_USER"));
        when(authentication.getPrincipal()).thenReturn("testUser");

        boolean result = preAuthorizer.hasAuthority("ROLE_ADMIN", authentication);

        assertFalse(result);
    }
}
