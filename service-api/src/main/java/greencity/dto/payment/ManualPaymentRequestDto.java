package greencity.dto.payment;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

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
