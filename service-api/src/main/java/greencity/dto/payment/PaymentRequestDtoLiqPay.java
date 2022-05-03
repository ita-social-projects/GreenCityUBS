package greencity.dto.payment;

import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PaymentRequestDtoLiqPay {
    private String publicKey;
    private Integer version;
    private String action;
    private Integer amount;
    private String currency;
    private String description;
    @Length(max = 255)
    private String orderId;
    private String language;
    private String paytypes;
    private String resultUrl;
}
