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
    private Long[] receivingStation;
    private Long[] responsibleCallerId;
    private Long[] responsibleDriverId;
    private Long[] responsibleNavigatorId;
    private Long[] responsibleLogicManId;
    private String orderDateFrom;
    private String orderDateTo;
    private String deliveryDateFrom;
    private String deliveryDateTo;
    private String paymentDateFrom;
    private String paymentDateTo;
    private String[] region;
    private String[] city;
    private String[] districts;
    private String search;
}
