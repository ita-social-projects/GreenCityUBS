package greencity.filters;

import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchCriteria {
    private OrderStatus[] orderStatus;
    private OrderPaymentStatus[] orderPaymentStatus;
    private String[] receivingStation;
    private Long[] responsibleCallerId;
    private Long[] responsibleDriverId;
    private Long[] responsibleNavigatorId;
    private Long[] responsibleLogiestManId;
    private String orderDateFrom;
    private String orderDateTo;
    private String deliverFromFrom;
    private String deliverFromTo;
    private String deliverToFrom;
    private String deliverToTo;
    private String paymentDateFrom;
    private String paymentDateTo;
    private String[] districts;
    private String search;
}
