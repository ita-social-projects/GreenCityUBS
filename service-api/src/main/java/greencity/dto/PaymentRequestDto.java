package greencity.dto;

import lombok.*;

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
    private String responseUrl;
}
