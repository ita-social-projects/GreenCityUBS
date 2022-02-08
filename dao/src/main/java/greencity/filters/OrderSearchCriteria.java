package greencity.filters;

import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import lombok.*;

@Data
public class OrderSearchCriteria {
    private OrderStatus[] orderStatus;
    private OrderPaymentStatus[] orderPaymentStatus;
    private Long[] receivingStation;
    private Long[] responsibleCallerId;
    private Long[] responsibleDriverId;
    private Long[] responsibleNavigatorId;
    private Long[] responsibleLogicManId;
    private DateFilter orderDate;
    private DateFilter deliveryDate;
    private DateFilter paymentDate;
    private String[] region;
    private String[] city;
    private String[] districts;
    private String[] search;
}
