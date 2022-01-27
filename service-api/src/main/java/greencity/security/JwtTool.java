package greencity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

/**
 * Class that provides methods for working with JWT.
 *
 * @author Nazar Stasyuk && Yurii Koval.
 * @version 2.0
 */
@Slf4j
@Component
public class JwtTool {
    private final String accessTokenKey;

    /**
     * Constructor.
     */
    @Autowired
    public JwtTool(@Value("$ {greencity.token-key}") String accessTokenKey) {
        this.accessTokenKey = accessTokenKey;
    }

    /**
     * Returns access token key.
     *
     * @return accessTokenKey
     */
    public String getAccessTokenKey() {
        return accessTokenKey;
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
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("authorities", Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, ttl);
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(calendar.getTime())
            .signWith(SignatureAlgorithm.HS256, accessTokenKey)
            .compact();
    }
}
