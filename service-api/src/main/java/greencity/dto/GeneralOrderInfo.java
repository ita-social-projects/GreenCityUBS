package greencity.dto;

import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import lombok.*;
import java.util.List;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class GeneralOrderInfo {
    private Long id;
    private LocalDateTime dateFormed;
    private List<OrderStatusesTranslationDto> orderStatusesDtos;
    private List<OrderPaymentStatusesTranslationDto> orderPaymentStatusesDto;
    private OrderStatus orderStatus;
    private String orderStatusName;
    private OrderPaymentStatus orderPaymentStatus;
    private String orderPaymentStatusName;
    private String adminComment;
}
