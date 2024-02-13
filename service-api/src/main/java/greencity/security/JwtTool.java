package greencity.security;

import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Date;

/**
 * Class that provides methods for working with JWT.
 *
 * @author Nazar Stasyuk && Yurii Koval.
 * @version 2.0
 */
@Slf4j
@Component
@Getter
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
     * @param email this is email of user.
     * @param ttl   is token time to live.
     */
    public String createAccessToken(String email, int ttl) {
        ClaimsBuilder claims = Jwts.claims().subject(email);
        claims.add("role", Arrays.asList("ROLE_USER", "ROLE_ADMIN"));

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, ttl);
        return Jwts.builder()
            .claims(claims.build())
            .issuedAt(now)
            .expiration(calendar.getTime())
            .signWith(Keys.hmacShaKeyFor(accessTokenKey.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
            .compact();
    }

    /**
     * Method for getting employee authorities from access token.
     *
     */
    @SuppressWarnings({"unchecked, rawtype"})
    public List<String> getAuthoritiesFromToken(String accessToken) {
        SecretKey key = Keys.hmacShaKeyFor(accessTokenKey.getBytes());
        return (List<String>) Jwts.parser()
            .verifyWith(key).build()
            .parseSignedClaims(accessToken)
            .getPayload()
            .get("employee_authorities");
    }
}
