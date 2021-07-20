package greencity.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class OrderPaymentDetailDto {
    private Long amount;
    private Integer certificates;
    private Integer pointsToUse;
    private Long amountToPay;
    private String currency;
}
