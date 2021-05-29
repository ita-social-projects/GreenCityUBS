package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PaymentRequestDto {
    private String orderId;
    private Integer merchantId;
    private String orderDescription;
    private String currency;
    private Integer amount;
    private String signature;
}
