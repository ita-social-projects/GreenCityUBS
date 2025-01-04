package greencity.dto.order;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class WayForPayPaymentResponse {
    private Long orderId;
    private String link;
}
