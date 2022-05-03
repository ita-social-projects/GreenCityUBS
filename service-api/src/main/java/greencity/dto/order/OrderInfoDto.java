package greencity.dto.order;

import greencity.entity.enums.OrderStatus;
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
