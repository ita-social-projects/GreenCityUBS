package greencity.dto.order;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class OrderDetailInfoDto {
    Long orderId;
    Integer capacity;
    Integer price;
    Integer amount;
    Integer exportedQuantity;
    Integer confirmedQuantity;
    String name;
    Integer bagId;
}
