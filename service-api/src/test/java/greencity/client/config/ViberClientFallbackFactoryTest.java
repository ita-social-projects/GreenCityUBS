package greencity.client.config;

import greencity.client.ViberClient;
import greencity.dto.viber.dto.SendMessageToUserDto;
import greencity.dto.viber.dto.WebhookDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ViberClientFallbackFactoryTest {
    @InjectMocks
    private ViberClientFallbackFactory fallbackFactory;

    private ViberClient client;

    @BeforeEach
    void setUp() {
        Throwable throwable = new RuntimeException();
        client = fallbackFactory.create(throwable);
    }

    @Test
    void getAccountInfo() {
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, client.getAccountInfo().getStatusCode());
    }

    @Test
    void updateWebHook() {
        WebhookDto dto = WebhookDto.builder().build();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, client.updateWebHook(dto).getStatusCode());
    }

    @Test
    void sendMessage() {
        SendMessageToUserDto dto = SendMessageToUserDto.builder().build();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, client.sendMessage(dto).getStatusCode());
    }
}