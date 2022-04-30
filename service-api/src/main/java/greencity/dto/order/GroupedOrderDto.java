package greencity.dto.order;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class GroupedOrderDto {
    private Integer amountOfLitres;
    private List<OrderDto> groupOfOrders;
}