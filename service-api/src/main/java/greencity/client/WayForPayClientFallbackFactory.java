package greencity.client;

import feign.hystrix.FallbackFactory;
import greencity.constant.ErrorMessage;
import greencity.exceptions.http.RemoteServerUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class WayForPayClientFallbackFactory implements FallbackFactory<WayForPayClient> {
    @Override
    public WayForPayClient create(Throwable throwable) {
        return dto -> {
            throw new RemoteServerUnavailableException(ErrorMessage.COULD_NOT_RETRIEVE_CHECKOUT_RESPONSE, throwable);
        };
    }
}
