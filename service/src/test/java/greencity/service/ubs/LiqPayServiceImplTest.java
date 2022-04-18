package greencity.service.ubs;

import com.liqpay.LiqPay;
import greencity.dto.PaymentRequestDtoLiqPay;
import greencity.dto.StatusRequestDtoLiqPay;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class LiqPayServiceImplTest {
    @InjectMocks
    LiqPayServiceImpl liqPayService;
    @Mock
    LiqPay liqPay;

    @Test
    void getData() {
        PaymentRequestDtoLiqPay dto = PaymentRequestDtoLiqPay.builder()
            .publicKey("")
            .version(3)
            .action("pay")
            .amount(100)
            .currency("UAH")
            .description("ubs courier")
            .orderId("1_1")
            .language("en")
            .paytypes("card")
            .resultUrl("")
            .build();
        liqPayService.getCheckoutResponse(dto);
        verify(liqPay).cnb_form(anyMap());
    }

    @Test
    @SneakyThrows
    void getStatus() {
        StatusRequestDtoLiqPay dto = StatusRequestDtoLiqPay.builder()
            .publicKey("")
            .action("status")
            .orderId("1_1")
            .version(3)
            .build();
        liqPayService.getPaymentStatus(dto);
        verify(liqPay).api(anyString(), anyMap());
    }
}