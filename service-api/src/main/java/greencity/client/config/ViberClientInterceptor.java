package greencity.client.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import greencity.client.ViberClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Class for setting header auth token and content type for
 * {@link ViberClient}'s requests.
 *
 * @author Andrii Yezenitskyi
 */
public class ViberClientInterceptor implements RequestInterceptor {
    private static final String TOKEN_HEADER_NAME = "X-Viber-Auth-Token";
    @Value("${greencity.bots.viber-bot-token}")
    private String viberBotToken;

    /**
     * Sets auth token and content type for request.
     *
     * @param template {@link RequestTemplate} of {@link ViberClient}.
     */
    @Override
    public void apply(RequestTemplate template) {
        template.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        template.header(TOKEN_HEADER_NAME, viberBotToken);
    }
}