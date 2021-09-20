package greencity.mapping;

import greencity.dto.PaymentResponseDto;
import greencity.entity.enums.PaymentStatus;
import greencity.entity.order.Payment;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper extends AbstractConverter<PaymentResponseDto, Payment> {
    /**
     * Method convert {@link PaymentResponseDto} to {@link Payment}.
     *
     * @return {@link Payment}
     */
    @Override
    protected Payment convert(PaymentResponseDto dto) {
        return Payment.builder()
            .id(Long.valueOf(dto.getOrderId().substring(dto.getOrderId().indexOf("_") + 1)))
            .currency(dto.getCurrency())
            .amount(Long.valueOf(dto.getAmount()))
            .orderStatus(dto.getOrderStatus())
            .responseStatus(dto.getResponseStatus())
            .senderCellPhone(dto.getSenderCellPhone())
            .senderAccount(dto.getSenderAccount())
            .maskedCard(dto.getMaskedCard())
            .cardType(dto.getCardType())
            .responseCode(dto.getResponseCode())
            .responseDescription(dto.getResponseDescription())
            .orderTime(dto.getOrderTime())
            .settlementDate(dto.getSettlementDate())
            .fee(Long.valueOf(dto.getFee()))
            .paymentSystem(dto.getPaymentSystem())
            .senderEmail(dto.getSenderEmail())
            .paymentId(Long.valueOf(dto.getPaymentId()))
            .paymentStatus(PaymentStatus.UNPAID)
            .build();
    }
}
