package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualPaymentRequestDto {
    private String settlementDate;
    private Long amount;
    private String paymentId;
    private String receiptLink;
    private String imagePath;
}
