package greencity.dto;

import java.util.List;
import lombok.*;

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