package greencity.mapping.payment;

import greencity.constant.AppConstant;
import greencity.dto.payment.PaymentWithStatusDto;
import greencity.entity.order.Payment;
import java.math.BigDecimal;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class PaymentWithStatusMapper extends AbstractConverter<Payment, PaymentWithStatusDto> {
    @Override
    protected PaymentWithStatusDto convert(Payment source) {
        return PaymentWithStatusDto.builder()
            .id(source.getId())
            .amount(BigDecimal.valueOf(source.getAmount())
                .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
                .doubleValue())
            .paymentStatus(source.getPaymentStatus())
            .build();
    }
}
