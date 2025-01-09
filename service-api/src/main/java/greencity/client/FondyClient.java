package greencity.client;

import greencity.client.config.FondyClientFallbackFactory;
import greencity.dto.payment.PaymentRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Client for interacting with Fondy API.
 *
 * @author Andrii Yezenitskyi
 */
@FeignClient(name = "fondy-client",
    url = "${greencity.external.fondy-api-url}",
    fallbackFactory = FondyClientFallbackFactory.class)
public interface FondyClient {
    /**
     * Returns response from Fondy checkout. <br>
     * Response contains: "response_status" ("success" or "failure") and
     * "checkout_url" in case of "success" or "error_message" in case of "failure".
     *
     * @param dto {@link PaymentRequestDto} payment details.
     * @return {@link String} raw JSON.
     */
    @PostMapping("/checkout/url")
    String getCheckoutResponse(@RequestBody PaymentRequestDto dto);
}
