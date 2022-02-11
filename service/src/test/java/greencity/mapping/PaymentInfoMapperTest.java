package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.PaymentInfoDto;
import greencity.entity.order.Payment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class PaymentInfoMapperTest {
    @InjectMocks
    private PaymentInfoMapper paymentInfoMapper;

    @Test
    void convert() {
        Payment payment = ModelUtils.getPayment();
        PaymentInfoDto expected = PaymentInfoDto.builder()
            .id(payment.getId())
            .paymentId(payment.getPaymentId())
            .amount(payment.getAmount())
            .build();
        PaymentInfoDto actual = paymentInfoMapper.convert(payment);

        assertEquals(expected, actual);
    }
}
