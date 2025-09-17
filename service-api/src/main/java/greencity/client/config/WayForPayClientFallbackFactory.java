package greencity.client.config;

import feign.hystrix.FallbackFactory;
import greencity.client.WayForPayClient;
import greencity.constant.ErrorMessage;
import greencity.dto.payment.PaymentCancellationWayForPayRequestDto;
import greencity.dto.payment.PaymentWayForPayRequestDto;
import greencity.exceptions.http.RemoteServerUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class WayForPayClientFallbackFactory implements FallbackFactory<WayForPayClient> {
    @Override
    public WayForPayClient create(Throwable cause) {
        return new WayForPayClient() {
            @Override
            public String getCheckOutResponse(PaymentWayForPayRequestDto dto) {
                throw new RemoteServerUnavailableException(
                    ErrorMessage.COULD_NOT_RETRIEVE_CHECKOUT_RESPONSE, cause);
            }

            @Override
            public String getCancellationResponse(PaymentCancellationWayForPayRequestDto dto) {
                throw new RemoteServerUnavailableException(
                    ErrorMessage.COULD_NOT_RETRIEVE_CANCELLATION_RESPONSE, cause);
            }
        };
    }
}
