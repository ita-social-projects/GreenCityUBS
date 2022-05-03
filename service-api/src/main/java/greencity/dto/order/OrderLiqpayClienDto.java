package greencity.dto.order;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class OrderLiqpayClienDto {
    private Long orderId;
    private Integer sum;
}
