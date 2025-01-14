package greencity.mapping.payment;

import greencity.ModelUtils;
import greencity.constant.AppConstant;
import greencity.dto.payment.PaymentInfoDto;
import greencity.entity.order.Payment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PaymentInfoMapperTest {
    @InjectMocks
    private PaymentInfoMapper paymentInfoMapper;

    @Test
    void convert() {
        Payment payment = ModelUtils.getPayment();
        PaymentInfoDto expected = PaymentInfoDto.builder()
            .id(payment.getId())
            .paymentId(payment.getPaymentId())
            .amount(BigDecimal.valueOf(payment.getAmount())
                .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
                .doubleValue())
            .settlementdate(LocalDate.now().toString())
            .build();
        PaymentInfoDto actual = paymentInfoMapper.convert(payment);

        assertEquals(expected, actual);
    }
}
