package greencity.client.config;

import greencity.client.MonoBankClient;
import greencity.dto.payment.monobank.MonoBankPaymentRequestDto;
import greencity.exceptions.http.RemoteServerUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class MonoBankClientFallbackFactoryTest {
    @InjectMocks
    private MonoBankClientFallbackFactory fallbackFactory;
    @Mock
    private MonoBankClient client;
    private String token;

    @BeforeEach
    void setUp() {
        Throwable throwable = new RuntimeException();
        client = fallbackFactory.create(throwable);
        token = "testToken";
    }

    @Test
    void getCheckoutResponse() {
        MonoBankPaymentRequestDto dto = MonoBankPaymentRequestDto.builder().build();
        assertThrows(RemoteServerUnavailableException.class, () -> client.getCheckoutResponse(dto, token));
    }
}
