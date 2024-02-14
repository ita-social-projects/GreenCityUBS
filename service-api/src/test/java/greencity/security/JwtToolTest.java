package greencity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtToolTest {
    @Mock
    private HttpServletRequest mockHttpServletRequest;

    private JwtTool jwtTool;

    @BeforeEach
    public void setup() {
        jwtTool = new JwtTool("secret-refresh-token-key-bigger-key");
    }

    @Test
    void testGetAccessTokenKey() {
        String accessTokenKey = jwtTool.getAccessTokenKey();
        assertEquals("secret-refresh-token-key-bigger-key", accessTokenKey);
    }

    @Test
    void testGetTokenFromHttpServletRequest() {
        when(mockHttpServletRequest.getHeader("Authorization")).thenReturn("Bearer testToken");
        String token = jwtTool.getTokenFromHttpServletRequest(mockHttpServletRequest);
        assertEquals("testToken", token);
        verify(mockHttpServletRequest).getHeader("Authorization");
    }

    @Test
    void testGetTokenFromHttpServletRequest_NullToken() {
        when(mockHttpServletRequest.getHeader("Authorization")).thenReturn(null);
        String token = jwtTool.getTokenFromHttpServletRequest(mockHttpServletRequest);
        assertNull(token);
        verify(mockHttpServletRequest).getHeader("Authorization");
    }

    @Test
    void testCreateAccessToken() {
        String email = "test@example.com";
        int ttl = 60;
        String accessToken = jwtTool.createAccessToken(email, ttl);
        SecretKey key = Keys.hmacShaKeyFor(jwtTool.getAccessTokenKey().getBytes());
        assertNotNull(accessToken);

        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(accessToken).getPayload();

        // Verify the subject (email) claim
        assertEquals(email, claims.getSubject());

        // Verify the role claim
        List<String> roles = (List<String>) claims.get("role");
        assertEquals(Arrays.asList("ROLE_USER", "ROLE_ADMIN"), roles);

        // Verify the expiration time
        Date expiration = claims.getExpiration();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expiration);
        calendar.add(Calendar.MINUTE, -ttl);
        Date expectedExpiration = calendar.getTime();
        assertEquals(expectedExpiration, claims.getIssuedAt());
    }

    @Test
    void testGetAuthoritiesFromToken() {
        final String accessToken = jwtTool.createAccessToken("test@example.com", 60);
        SecretKey key = Keys.hmacShaKeyFor(jwtTool.getAccessTokenKey().getBytes());

        @SuppressWarnings({"unchecked, rawtype"})
        List<String> authorities = (List<String>) Jwts.parser()
            .verifyWith(key).build()
            .parseSignedClaims(accessToken)
            .getPayload()
            .get("role");
        List<String> expectedAuthorities = Arrays.asList("ROLE_USER", "ROLE_ADMIN");
        assertEquals(expectedAuthorities, authorities);
    }
}
