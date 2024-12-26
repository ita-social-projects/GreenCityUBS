package greencity.client;

import greencity.client.config.WayForPayClientFallbackFactory;
import greencity.dto.payment.PaymentWayForPayRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wayforpay-client",
    url = "https://api.wayforpay.com/api",
    fallbackFactory = WayForPayClientFallbackFactory.class)
public interface WayForPayClient {
    /**
     * Sends a POST request to the WayForPay API to get a checkout response.
     *
     * @param dto The payment request data.
     * @return A string representing the response from the WayForPay API.
     */
    @PostMapping
    String getCheckOutResponse(@RequestBody PaymentWayForPayRequestDto dto);
}
