package greencity.mapping;

import greencity.dto.PaymentInfoDto;
import greencity.entity.order.Payment;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

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
            .amount(source.getAmount())
            .settlementdate(source.getSettlementDate())
            .comment(source.getComment())
            .receiptLink(source.getReceiptLink())
            .imagePath(source.getImagePath())
            .build();
    }
}
