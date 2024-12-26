package greencity.client.config;

import feign.hystrix.FallbackFactory;
import greencity.client.WayForPayClient;
import greencity.constant.ErrorMessage;
import greencity.exceptions.http.RemoteServerUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class WayForPayClientFallbackFactory implements FallbackFactory<WayForPayClient> {
    @Override
    public WayForPayClient create(Throwable cause) {
        return dto -> {
            throw new RemoteServerUnavailableException(ErrorMessage.COULD_NOT_RETRIEVE_CHECKOUT_RESPONSE, cause);
        };
    }
}
