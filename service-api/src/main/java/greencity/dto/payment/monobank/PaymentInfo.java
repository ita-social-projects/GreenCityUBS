package greencity.dto.payment.monobank;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PaymentInfo {
    @JsonProperty("maskedPan")
    private String cardNumber;
    private String terminal;
    private String paymentSystem;
    private String paymentMethod;
    private Integer fee;
}
