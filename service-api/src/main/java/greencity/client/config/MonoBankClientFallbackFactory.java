package greencity.client.config;

import feign.hystrix.FallbackFactory;
import greencity.client.MonoBankClient;
import greencity.constant.ErrorMessage;
import greencity.exceptions.http.RemoteServerUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class MonoBankClientFallbackFactory implements FallbackFactory<MonoBankClient> {
    @Override
    public MonoBankClient create(Throwable cause) {
        return (requestDto, token) -> {
            throw new RemoteServerUnavailableException(ErrorMessage.COULD_NOT_RETRIEVE_CHECKOUT_RESPONSE, cause);
        };
    }
}
