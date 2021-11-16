package greencity.dto;

import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.Payment;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class UserOrdersDto {
    private Long id;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private OrderPaymentStatus orderPaymentStatus;
    private Long amount;
}
