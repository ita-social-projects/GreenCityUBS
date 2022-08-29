package greencity.dto.order;

import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class GeneralOrderInfo {
    private Long id;
    private ZonedDateTime dateFormed;
    private List<OrderStatusesTranslationDto> orderStatusesDtos;
    private List<OrderPaymentStatusesTranslationDto> orderPaymentStatusesDto;
    private OrderStatus orderStatus;
    private String orderStatusName;
    private String orderStatusNameEng;
    private OrderPaymentStatus orderPaymentStatus;
    private String orderPaymentStatusName;
    private String orderPaymentStatusNameEng;
    private String adminComment;
}
