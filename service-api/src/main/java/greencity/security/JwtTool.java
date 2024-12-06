package greencity.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Class that provides methods for working with JWT.
 *
 * @author Nazar Stasyuk && Yurii Koval.
 * @version 3.0
 */
@Getter
@Slf4j
@Component
public class JwtTool {
    private final String accessTokenKey;

    /**
     * Constructor.
     */
    @Autowired
    public JwtTool(@Value("${greencity.authorization.token-key}") String accessTokenKey) {
        this.accessTokenKey = accessTokenKey;
    }

    /**
     * Method that get token from {@link HttpServletRequest}.
     *
     * @param servletRequest this is your request.
     * @return {@link String} of token or null.
     */
    public String getTokenFromHttpServletRequest(HttpServletRequest servletRequest) {
        return Optional
            .ofNullable(servletRequest.getHeader("Authorization"))
            .filter(authHeader -> authHeader.startsWith("Bearer "))
            .map(token -> token.substring(7))
            .orElse(null);
    }

    /**
     * Method for creating access token.
     *
     * @param email — email of user.
     * @param ttl   — token time to live in minutes.
     */
    public String createAccessToken(String email, int ttl) {
        Instant now = Instant.now();
        Instant expiration = now.plus(ttl, ChronoUnit.MINUTES);

        byte[] keyBytes = Decoders.BASE64.decode(accessTokenKey);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
            .subject(email)
            .claim("role", Arrays.asList("ROLE_USER", "ROLE_ADMIN"))
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(key)
            .compact();
    }

    /**
     * Method for getting employee authorities from access token.
     */
    @SuppressWarnings("unchecked")
    public List<String> getAuthoritiesFromToken(String accessToken) {
        byte[] keyBytes = Decoders.BASE64.decode(accessTokenKey);
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);

        return (List<String>) Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(accessToken)
            .getPayload()
            .get("employee_authorities");
    }
}
