package greencity.dto;

import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.Certificate;
import lombok.*;

import java.util.Map;
import java.util.Set;
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
