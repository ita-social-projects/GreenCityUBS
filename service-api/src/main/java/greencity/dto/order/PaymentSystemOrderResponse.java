package greencity.dto.order;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class PaymentSystemOrderResponse {
    private Long orderId;
    private String link;
}
