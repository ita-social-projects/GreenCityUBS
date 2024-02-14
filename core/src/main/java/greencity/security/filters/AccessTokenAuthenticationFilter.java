package greencity.security.filters;

import greencity.dto.user.UserVO;
import greencity.security.JwtTool;
import greencity.service.FeignClientCallAsync;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Class that provide Authentication object based on JWT.
 *
 * @author Yurii Koval.
 * @version 1.0
 */
@Slf4j
public class AccessTokenAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTool jwtTool;
    private final AuthenticationManager authenticationManager;
    private final FeignClientCallAsync userRemoteClient;

    /**
     * Constructor.
     */
    public AccessTokenAuthenticationFilter(JwtTool jwtTool, AuthenticationManager authenticationManager,
        FeignClientCallAsync userRemoteClient) {
        this.jwtTool = jwtTool;
        this.authenticationManager = authenticationManager;
        this.userRemoteClient = userRemoteClient;
    }

    private String extractToken(HttpServletRequest request) {
        return jwtTool.getTokenFromHttpServletRequest(request);
    }

    /**
     * Checks if request has token in header, if this token still valid, and set
     * authentication for spring.
     *
     * @param request  this is servlet that take request
     * @param response this is response servlet
     * @param chain    this is filter of chain
     */
    @Override
    public void doFilterInternal(@SuppressWarnings("NullableProblems") HttpServletRequest request,
        @SuppressWarnings("NullableProblems") HttpServletResponse response,
        @SuppressWarnings("NullableProblems") FilterChain chain)
        throws IOException, ServletException {
        String token = extractToken(request);

        log.info("token: {}", token);
        if (token != null) {
            try {
                ((ProviderManager) authenticationManager).setEraseCredentialsAfterAuthentication(false);
                Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(token, null));
                Optional<UserVO> user =
                    userRemoteClient.getRecordsAsync((String) authentication.getPrincipal()).get();
                log.info("user: {}", user);
                if (user.isPresent()) {
                    log.debug("User successfully authenticate - {}", authentication.getPrincipal());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (ExpiredJwtException e) {
                log.info("Token has expired: " + token);
            } catch (InterruptedException e) {
                log.info("Thread was interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.info("Access denied with token: " + e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }
}
