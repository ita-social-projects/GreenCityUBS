package greencity.mapping.payment;

import greencity.constant.AppConstant;
import greencity.dto.payment.PaymentInfoDto;
import greencity.entity.order.Payment;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class PaymentInfoMapper extends AbstractConverter<Payment, PaymentInfoDto> {
    /**
     * Method convert {@link PaymentInfoDto} to {@link Payment}.
     *
     * @return {@link PaymentInfoDto}
     */
    @Override
    protected PaymentInfoDto convert(Payment source) {
        return PaymentInfoDto.builder()
            .id(source.getId())
            .paymentId(source.getPaymentId())
            .amount(BigDecimal.valueOf(source.getAmount())
                .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
                .doubleValue())
            .settlementdate(source.getSettlementDate())
            .comment(source.getComment())
            .receiptLink(source.getReceiptLink())
            .imagePath(source.getImagePath())
            .build();
    }
}
