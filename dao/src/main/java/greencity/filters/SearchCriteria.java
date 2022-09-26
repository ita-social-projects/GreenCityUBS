package greencity.filters;

import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.enums.PaymentSystem;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchCriteria {
    private OrderStatus[] orderStatuses;
    private PaymentSystem[] paymentSystems;
    private OrderPaymentStatus[] orderPaymentStatuses;
    private String[] receivingStations;
    private String[] districts;
    private String orderDate;
    private String dateFrom;
    private String dateTo;
    private String searchValue;
}
