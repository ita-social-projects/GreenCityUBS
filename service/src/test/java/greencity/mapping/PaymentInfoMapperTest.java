package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.PaymentInfoDto;
import greencity.entity.order.Payment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentInfoMapperTest {

    @InjectMocks
    private PaymentInfoMapper paymentInfoMapper;

    @Test
    void convert() {

        Payment payment = ModelUtils.getManualPayment();

        PaymentInfoDto expected = PaymentInfoDto.builder()
            .paymentId("1l")
            .comment(null)
            .id(null)
            .imagePath("")
            .amount(500L)
            .receiptLink("somelink.com")
            .settlementdate("02-08-2021")
            .build();

        PaymentInfoDto actual = paymentInfoMapper.convert(payment);

        Assertions.assertEquals(actual.toString(), expected.toString());

    }

}
