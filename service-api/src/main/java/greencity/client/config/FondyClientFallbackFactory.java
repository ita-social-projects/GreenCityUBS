package greencity.client.config;

import feign.hystrix.FallbackFactory;
import greencity.client.FondyClient;
import greencity.constant.ErrorMessage;
import greencity.exceptions.http.RemoteServerUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class FondyClientFallbackFactory implements FallbackFactory<FondyClient> {
    @Override
    public FondyClient create(Throwable throwable) {
        return dto -> {
            throw new RemoteServerUnavailableException(ErrorMessage.COULD_NOT_RETRIEVE_CHECKOUT_RESPONSE, throwable);
        };
    }
}
