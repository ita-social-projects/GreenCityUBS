package greencity.client.config;

import greencity.client.FondyClient;
import greencity.dto.payment.PaymentRequestDto;
import greencity.exceptions.http.RemoteServerUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class FondyClientFallbackFactoryTest {
    @InjectMocks
    private FondyClientFallbackFactory fallbackFactory;

    private FondyClient client;

    @BeforeEach
    void setUp() {
        Throwable throwable = new RuntimeException();
        client = fallbackFactory.create(throwable);
    }

    @Test
    void getCheckoutResponse() {
        PaymentRequestDto dto = PaymentRequestDto.builder().build();
        assertThrows(RemoteServerUnavailableException.class, () -> client.getCheckoutResponse(dto));
    }
}