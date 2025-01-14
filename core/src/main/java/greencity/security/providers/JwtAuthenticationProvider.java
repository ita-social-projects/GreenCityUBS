package greencity.security.providers;

import greencity.security.JwtTool;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class that provides authentication logic.
 *
 * @author Yurii Koval
 * @version 1.1
 */
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {
    private final JwtTool jwtTool;

    /**
     * Method that provide authentication.
     *
     * @param authentication {@link Authentication} - authentication that has jwt
     *                       access token.
     * @return {@link Authentication} if user successfully authenticated.
     * @throws io.jsonwebtoken.ExpiredJwtException   - if the token expired.
     * @throws UnsupportedJwtException               if the argument does not
     *                                               represent a Claims JWS
     * @throws io.jsonwebtoken.MalformedJwtException if the string is not a valid
     *                                               JWS
     * @throws io.jsonwebtoken.SignatureException    if the JWS signature validation
     *                                               fails
     */
    @Override
    public Authentication authenticate(Authentication authentication) {
        SecretKey key = Keys.hmacShaKeyFor(jwtTool.getAccessTokenKey().getBytes());
        String email = Jwts.parser()
            .verifyWith(key).build()
            .parseSignedClaims(authentication.getName())
            .getPayload()
            .getSubject();
        @SuppressWarnings({"unchecked, rawtype"})
        List<String> role = (List<String>) Jwts.parser()
            .verifyWith(key).build()
            .parseSignedClaims(authentication.getName())
            .getPayload()
            .get("role");
        return new UsernamePasswordAuthenticationToken(
            email,
            role.contains("ROLE_UBS_EMPLOYEE")
                ? jwtTool.getAuthoritiesFromToken(authentication.getName())
                : Collections.emptyList(),
            role.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
