package greencity.dto.order;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class FondyOrderResponse {
    private Long orderId;
    private String link;
}
