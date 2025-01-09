package greencity.dto.payment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualPaymentRequestDto {
    @NotEmpty
    private String settlementdate;
    @NotNull
    @Positive
    private Long amount;
    @NotEmpty
    private String paymentId;
    private String receiptLink;
    private String imagePath;
}
