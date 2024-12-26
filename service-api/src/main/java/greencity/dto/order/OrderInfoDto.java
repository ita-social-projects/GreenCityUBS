package greencity.dto.order;

import greencity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
