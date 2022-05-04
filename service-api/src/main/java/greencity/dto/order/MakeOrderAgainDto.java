package greencity.dto.order;

import greencity.dto.bag.BagOrderDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class MakeOrderAgainDto {
    private Long orderId;
    private Long orderAmount;
    List<BagOrderDto> bagOrderDtoList;
}
