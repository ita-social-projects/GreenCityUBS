package greencity.client.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

import static feign.FeignException.errorStatus;

@Component
public class StashErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() >= 400 && response.status() <= 499) {
            return new RuntimeException(
                response.reason());
        }
        if (response.status() >= 500 && response.status() <= 599) {
            return new RuntimeException(
                response.reason());
        }
        return errorStatus(methodKey, response);
    }
}