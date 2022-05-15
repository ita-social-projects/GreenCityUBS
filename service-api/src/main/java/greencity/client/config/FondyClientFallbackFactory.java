package greencity.client.config;

import feign.hystrix.FallbackFactory;
import greencity.client.FondyClient;
import greencity.constant.ErrorMessage;
import greencity.dto.payment.PaymentRequestDto;
import greencity.exceptions.PaymentValidationException;
import org.springframework.stereotype.Component;

@Component
public class FondyClientFallbackFactory implements FallbackFactory<FondyClient> {
    @Override
    public FondyClient create(Throwable throwable) {
        return new FondyClient() {
            @Override
            public String getCheckoutResponse(PaymentRequestDto dto) {
                throw new PaymentValidationException(ErrorMessage.COULD_NOT_RETRIEVE_CHECKOUT_RESPONSE);
            }
        };
    }
}
