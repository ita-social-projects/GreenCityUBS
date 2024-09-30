package greencity.dto.payment.monobank;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PaymentInfo(
    @JsonProperty("maskedPan") String cardNumber,
    String terminal,
    String paymentSystem,
    String paymentMethod,
    Integer fee) {
}
