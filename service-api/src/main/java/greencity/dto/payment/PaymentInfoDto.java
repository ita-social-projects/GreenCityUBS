package greencity.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentInfoDto {
    Long id;
    String settlementdate;
    String paymentId;
    Double amount;
    String comment;
    String receiptLink;
    String imagePath;
}
