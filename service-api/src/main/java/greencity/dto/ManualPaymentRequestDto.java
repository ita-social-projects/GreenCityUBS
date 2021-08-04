package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualPaymentRequestDto {
    private String paymentDate;
    private Long amount;
    private Long paymentId;
    private String receiptLink;
}
