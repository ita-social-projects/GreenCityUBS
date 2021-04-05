package greencity.mapping;

import greencity.dto.PaymentResponseDto;
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
            .id(Long.valueOf(dto.getOrder_id()))
            .currency(dto.getCurrency())
            .amount(Long.valueOf(dto.getAmount()))
            .orderStatus(dto.getOrder_status())
            .responseStatus(dto.getResponse_status())
            .senderCellPhone(dto.getSender_cell_phone())
            .senderAccount(dto.getSender_account())
            .maskedCard(dto.getMasked_card())
            .cardType(dto.getCard_type())
            .responseCode(dto.getResponse_code())
            .responseDescription(dto.getResponse_description())
            .orderTime(dto.getOrder_time())
            .settlementDate(dto.getSettlement_date())
            .fee(Long.valueOf(dto.getFee()))
            .paymentSystem(dto.getPayment_system())
            .senderEmail(dto.getSender_email())
            .paymentId(Long.valueOf(dto.getPayment_id()))
            .build();
    }
}
