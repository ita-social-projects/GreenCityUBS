package greencity.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualPaymentResponseDto {
    private Long id;
    private String settlementdate;
    private Long amount;
    private String paymentId;
    private String receiptLink;
    private String imagePath;
    private String currentDate;
}
