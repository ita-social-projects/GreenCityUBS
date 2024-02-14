package greencity.filters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
