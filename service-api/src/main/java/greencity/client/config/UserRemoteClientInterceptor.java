package greencity.client.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import greencity.client.UserRemoteClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Class for setting <em>Authorization</em> header for
 * {@link UserRemoteClient}'s requests.
 *
 * @author Andrii Yezenitskyi
 */
public class UserRemoteClientInterceptor implements RequestInterceptor {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ATTRIBUTE_EXTRACTION_FAILED = "Request attribute extraction failed";

    /**
     * Sets <em>Authorization</em> header with access token for request.
     *
     * @param template {@link RequestTemplate} of {@link UserRemoteClient}.
     */
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes servletRequestAttributes =
            Optional.ofNullable(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()))
                .orElseThrow(() -> new RuntimeException(ATTRIBUTE_EXTRACTION_FAILED));
        String accessToken = servletRequestAttributes.getRequest().getHeader(AUTHORIZATION_HEADER);
        template.header(AUTHORIZATION_HEADER, accessToken);
    }
}
