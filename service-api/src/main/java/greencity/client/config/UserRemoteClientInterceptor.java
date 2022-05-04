package greencity.client.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import greencity.client.UserRemoteClient;
import greencity.security.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Class for setting <em>Authorization</em> header for
 * {@link UserRemoteClient}'s requests.
 *
 * @author Andrii Yezenitskyi
 */
@RequiredArgsConstructor
public class UserRemoteClientInterceptor implements RequestInterceptor {
    private final JwtTool jwtTool;
    @Value("${greencity.authorization.service-email}")
    private String serviceEmail;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";

    /**
     * Sets <em>Authorization</em> header with access token for request.
     *
     * @param template {@link RequestTemplate} of {@link UserRemoteClient}.
     */
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes servletRequestAttributes =
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());

        String accessToken = servletRequestAttributes != null
            ? servletRequestAttributes.getRequest().getHeader(AUTHORIZATION_HEADER)
            : createAccessTokenForService();

        template.header(AUTHORIZATION_HEADER, accessToken);
    }

    /**
     * Creates JWT for service-to-service communication.
     *
     * @return {@link String} - access token.
     */
    private String createAccessTokenForService() {
        return BEARER + jwtTool.createAccessToken(serviceEmail, 1);
    }
}
