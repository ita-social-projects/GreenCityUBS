package greencity.dto.payment;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class FondyPaymentResponse {
    private String paymentStatus;
}
