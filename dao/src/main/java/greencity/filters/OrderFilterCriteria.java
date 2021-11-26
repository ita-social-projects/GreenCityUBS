package greencity.filters;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class OrderFilterCriteria {
    private String[] orderDate;
    private String[] id;
    private String[] orderStatus;
    private String[] orderPaymentStatus;
    private String[] priceOfOrder;
}
