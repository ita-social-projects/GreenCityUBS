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
    private String[] responsibleCallerFirstName;
    private String[] responsibleCallerLastName;
    private String[] receivingStation;
    private String[] districts;
    private String dateFrom;
    private String dateTo;
    private String search;
}
