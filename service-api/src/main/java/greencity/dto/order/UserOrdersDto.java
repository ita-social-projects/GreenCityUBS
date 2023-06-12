package greencity.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class UserOrdersDto {
    private Long id;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private OrderPaymentStatus orderPaymentStatus;
    private Double amount;
}
