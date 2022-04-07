package greencity.service;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.hibernate.jpa.internal.util.PersistenceUtilHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Class for setting <em>Authorization</em> header for
 * {@link UserRemoteService}'s requests.
 *
 * @author Andrii Yezenitskyi
 */
@Component
@RequiredArgsConstructor
public class UserRemoteServiceAuthorizationSetter implements RequestInterceptor {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ATTRIBUTE_EXTRACTION_FAILED = "Attribute extraction failed";

    /**
     * Sets <em>Authorization</em> header with access token for request.
     *
     * @param template {@link RequestTemplate} of {@link UserRemoteService}.
     */
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes servletRequestAttributes =
            Optional.ofNullable(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()))
                .orElseThrow(() -> new PersistenceUtilHelper.AttributeExtractionException(ATTRIBUTE_EXTRACTION_FAILED));
        String accessToken = servletRequestAttributes.getRequest().getHeader(AUTHORIZATION_HEADER);
        template.header(AUTHORIZATION_HEADER, accessToken);
    }
}
