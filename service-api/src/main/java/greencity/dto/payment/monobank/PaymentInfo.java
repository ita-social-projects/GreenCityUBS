package greencity.dto.payment.monobank;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PaymentInfo {
    @JsonProperty("maskedPan")
    private String cardNumber;
    private String terminal;
    private String paymentSystem;
    private String paymentMethod;
    private Integer fee;
}
