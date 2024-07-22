package greencity.client.config;

import greencity.client.WayForPayClient;
import greencity.dto.payment.PaymentRequestDto;
import greencity.exceptions.http.RemoteServerUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class WayForPayClientFallbackFactoryTest {
    @InjectMocks
    private WayForPayClientFallbackFactory fallbackFactory;
    @Mock
    private WayForPayClient client;

    @BeforeEach
    void setUp() {
        Throwable throwable = new RuntimeException();
        client = fallbackFactory.create(throwable);
    }

    @Test
    void getCheckoutResponse() {
        PaymentRequestDto dto = PaymentRequestDto.builder().build();
        assertThrows(RemoteServerUnavailableException.class, () -> client.getCheckOutResponse(dto));
    }
}
