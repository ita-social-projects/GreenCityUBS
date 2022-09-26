package greencity.dto.order;

import greencity.enums.OrderStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class OrderInfoDto {
    private Long id;
    private OrderStatus orderStatus;
    private double orderPrice;
}
