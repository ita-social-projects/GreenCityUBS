package greencity.dto.payment;

import greencity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentWithStatusDto {
    Long id;
    Double amount;
    PaymentStatus paymentStatus;
}
