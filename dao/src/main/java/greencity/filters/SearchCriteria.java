package greencity.filters;

import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentSystem;
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
