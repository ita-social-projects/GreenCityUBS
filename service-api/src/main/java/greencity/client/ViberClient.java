package greencity.client;

import greencity.client.config.ViberClientInterceptor;
import greencity.dto.viber.dto.SendMessageToUserDto;
import greencity.dto.viber.dto.WebhookDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Client for interacting with Viber API.
 *
 * @author Andrii Yezenitskyi
 */
@FeignClient(name = "viber-client",
    url = "${greencity.external.viber-api-url}",
    configuration = ViberClientInterceptor.class)
public interface ViberClient {
    /**
     * Returns account info.
     *
     * @return {@link String} - raw JSON with status and additional details.
     */
    @PostMapping("/get_account_info")
    ResponseEntity<String> getAccountInfo();

    /**
     * Sets the URL of the Viber bot to the one specified in request.
     *
     * @param dto {@link WebhookDto} request dto.
     *
     * @return {@link String} - raw JSON with status and additional details.
     */
    @PostMapping("/set_webhook")
    ResponseEntity<String> updateWebHook(WebhookDto dto);

    /**
     * Sends a message to user.
     *
     * @param message {@link SendMessageToUserDto}
     *
     * @return {@link String} - raw JSON with status and additional details.
     */
    @PostMapping("/send_message")
    ResponseEntity<String> sendMessage(SendMessageToUserDto message);
}
