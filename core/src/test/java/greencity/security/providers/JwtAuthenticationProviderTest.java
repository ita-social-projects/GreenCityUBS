package greencity.security.providers;

import greencity.security.JwtTool;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author Yurii Koval
 */
class JwtAuthenticationProviderTest {

    enum Role {
        ROLE_ADMIN
    }

    private final Role expectedRole = Role.ROLE_ADMIN;

    @Mock
    JwtTool jwtTool;

    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtTool);
    }

    @Test
    void authenticateWithValidAccessToken() {
        final String accessToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJxcXFAZW1haWwu"
                + "Y29tIiwicm9sZSI6WyJST0xFX0FETUlOIl0sImlhdCI6MTY1NDYzNjc2OSwiZXh"
                + "wIjo2MTY1NDYzNjcwOX0.Ug-epWHV0a9f7BFPa1geKhqWysWkOdoG5wd4h2Hzpi4";
        when(jwtTool.getAccessTokenKey()).thenReturn("123123123");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            accessToken,
            null);
        Authentication actual = jwtAuthenticationProvider.authenticate(authentication);
        final String expectedEmail = "qqq@email.com";
        assertEquals(expectedEmail, actual.getPrincipal());
        assertEquals(
            Stream.of(expectedRole)
                .map(Role::toString)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()),
            actual.getAuthorities());
        assertEquals(Collections.emptyList(), actual.getCredentials());
    }

    @Test
    void authenticateWithExpiredAccessToken() {
        when(jwtTool.getAccessTokenKey()).thenReturn("123123123");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "eyJhbGciOiJIUzI1NiJ9"
                + ".eyJzdWIiOiJ0ZXN0QGdtYWlsLmNvbSIsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE1Nz"
                + "U4Mzk3OTMsImV4cCI6MTU3NTg0MDY5M30"
                + ".DYna1ycZd7eaUBrXKGzYvEMwcybe7l5YiliOR-LfyRw",
            null);
        Assertions
            .assertThrows(ExpiredJwtException.class,
                () -> jwtAuthenticationProvider.authenticate(authentication));
    }

    @Test
    void authenticateWithMalformedAccessToken() {
        when(jwtTool.getAccessTokenKey()).thenReturn("123123123");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "Malformed"
                + ".eyJzdWIiOiJ0ZXN0QGdtYWlsLmNvbSIsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE1Nz"
                + "U4Mzk3OTMsImV4cCI6MTU3NTg0MDY5M30"
                + ".DYna1ycZd7eaUBrXKGzYvEMwcybe7l5YiliOR-LfyRw",
            null);
        Assertions
            .assertThrows(Exception.class,
                () -> jwtAuthenticationProvider.authenticate(authentication));
    }

    @Test
    void supportsTest() {
        assertTrue(jwtAuthenticationProvider.supports(UsernamePasswordAuthenticationToken.class));
    }
}
