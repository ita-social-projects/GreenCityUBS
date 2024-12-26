package greencity.client;

import greencity.client.config.MonoBankClientFallbackFactory;
import greencity.dto.payment.monobank.CheckoutResponseFromMonoBank;
import greencity.dto.payment.monobank.MonoBankPaymentRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "monobank-client",
    url = "https://api.monobank.ua/api/merchant/invoice/create",
    fallbackFactory = MonoBankClientFallbackFactory.class)
public interface MonoBankClient {
    /**
     * Sends a POST request to the MonoBank API to get a checkout response.
     *
     * @param requestDto {@link MonoBankPaymentRequestDto} the payment request data.
     * @param token      The authorization token from MonoBank.
     * @return {@link CheckoutResponseFromMonoBank} representing the response from
     *         the MonoBank API.
     */
    @PostMapping
    CheckoutResponseFromMonoBank getCheckoutResponse(
        @RequestBody MonoBankPaymentRequestDto requestDto,
        @RequestHeader("X-Token") String token);
}
