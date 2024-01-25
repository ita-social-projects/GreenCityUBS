package greencity.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class OrderDetailInfoDto {
    Long orderId;
    Integer capacity;
    Double price;
    Integer amount;
    Integer exportedQuantity;
    Integer confirmedQuantity;
    String name;
    Integer bagId;
}
