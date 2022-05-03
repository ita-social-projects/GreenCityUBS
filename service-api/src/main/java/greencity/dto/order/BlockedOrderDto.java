package greencity.dto.order;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class BlockedOrderDto {
    private Long orderId;
    private String userName;
}
