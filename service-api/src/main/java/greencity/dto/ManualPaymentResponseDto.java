package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualPaymentResponseDto {
    private String paymentDate;
    private Long amount;
    private Long paymentId;
    private String receiptLink;
    private String imagePath;
    private String currentDate;
}
