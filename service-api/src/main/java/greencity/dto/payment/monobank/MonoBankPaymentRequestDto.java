package greencity.dto.payment.monobank;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record MonoBankPaymentRequestDto(
    Integer amount,
    @JsonProperty("merchantPaymInfo") MerchantPaymentInfo merchantPaymentInfo,
    String redirectUrl,
    String webHookUrl) {
}
