package greencity.dto.payment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualPaymentRequestDto {
    private String settlementdate;
    private Long amount;
    private String paymentId;
    private String receiptLink;
    private String imagePath;
}
