package greencity.dto.order;

import greencity.dto.bag.BagOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
