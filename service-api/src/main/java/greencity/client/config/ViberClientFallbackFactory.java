package greencity.client.config;

import feign.hystrix.FallbackFactory;
import greencity.client.ViberClient;
import greencity.constant.ErrorMessage;
import greencity.dto.viber.dto.SendMessageToUserDto;
import greencity.dto.viber.dto.WebhookDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ViberClientFallbackFactory implements FallbackFactory<ViberClient> {
    private static final String JSON_COULD_NOT_RETRIEVE_DATA = "{\"message\": \"Could not retrieve data\"}";
    private static final String JSON_COULD_NOT_SET_WEBHOOK = "{\"message\": \"Could not set webhook\"}";
    private static final String JSON_COULD_NOT_SEND_MESSAGE = "{\"message\": \"Could not send message\"}";

    @Override
    public ViberClient create(Throwable throwable) {
        return new ViberClient() {
            @Override
            public ResponseEntity<String> getAccountInfo() {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(JSON_COULD_NOT_RETRIEVE_DATA);
            }

            @Override
            public ResponseEntity<String> updateWebHook(WebhookDto dto) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(JSON_COULD_NOT_SET_WEBHOOK);
            }

            @Override
            public ResponseEntity<String> sendMessage(SendMessageToUserDto message) {
                log.error(ErrorMessage.THE_MESSAGE_WAS_NOT_SENT, throwable);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(JSON_COULD_NOT_SEND_MESSAGE);
            }
        };
    }
}
