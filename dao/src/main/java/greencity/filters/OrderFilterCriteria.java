package greencity.filters;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderFilterCriteria {
    private String[] orderDate;
    private String[] id;
    private String[] orderStatus;
    private String[] orderPaymentStatus;
    private String[] priceOfOrder;
}
