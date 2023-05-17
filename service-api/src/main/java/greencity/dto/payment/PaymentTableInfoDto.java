package greencity.dto.payment;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class PaymentTableInfoDto {
    Double paidAmount;
    Double unPaidAmount;
    List<PaymentInfoDto> paymentInfoDtos;
    Double overpayment;
}
