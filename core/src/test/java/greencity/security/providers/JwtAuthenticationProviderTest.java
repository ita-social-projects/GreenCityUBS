package greencity.security.providers;

import greencity.security.JwtTool;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
    private static final String expectedEmail = "qqq@email.com";
    private static final SecretKey secretKey = Jwts.SIG.HS256.key().build();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtTool);

        final String keyString = Encoders.BASE64.encode(secretKey.getEncoded());
        when(jwtTool.getAccessTokenKey()).thenReturn(keyString);
    }

    @Test
    void authenticateWithValidAccessToken() {
        String accessToken = Jwts.builder()
            .signWith(secretKey)
            .subject(expectedEmail)
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plus(30, ChronoUnit.DAYS)))
            .claim("role", List.of(expectedRole)).compact();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            accessToken,
            null);
        Authentication actual = jwtAuthenticationProvider.authenticate(authentication);
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
        String accessToken = Jwts.builder()
            .signWith(secretKey)
            .subject(expectedEmail)
            .issuedAt(Date.from(Instant.now().minus(30, ChronoUnit.DAYS)))
            .expiration(Date.from(Instant.now().minus(24, ChronoUnit.DAYS)))
            .claim("role", List.of(expectedRole)).compact();
        Authentication authentication = new UsernamePasswordAuthenticationToken(accessToken, null);
        Assertions
            .assertThrows(ExpiredJwtException.class,
                () -> jwtAuthenticationProvider.authenticate(authentication));
    }

    @Test
    void authenticateWithMalformedAccessToken() {
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
