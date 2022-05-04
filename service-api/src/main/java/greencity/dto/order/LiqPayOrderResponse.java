package greencity.dto.order;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class LiqPayOrderResponse {
    private Long orderId;
    private String liqPayButton;
}
