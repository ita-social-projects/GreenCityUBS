package greencity.client;


import greencity.dto.payment.PaymentRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wayforpay-client",
        url = "https://secure.wayforpay.com/pay",
        fallbackFactory = WayForPayClientFallbackFactory.class)
public interface WayForPayClient {
    String getCheckoutResponse(@RequestBody PaymentRequestDto dto);
}

