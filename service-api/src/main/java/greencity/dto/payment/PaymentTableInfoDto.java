package greencity.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
