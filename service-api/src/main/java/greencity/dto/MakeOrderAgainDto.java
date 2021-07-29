package greencity.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class MakeOrderAgainDto {
    private Long orderId;
    private Long orderAmount;
    List<BagOrderDto> bagOrderDtoList;
}
