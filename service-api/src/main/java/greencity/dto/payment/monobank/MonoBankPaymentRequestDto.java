package greencity.dto.payment.monobank;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MonoBankPaymentRequestDto {
    private Integer amount;
    @JsonProperty("merchantPaymInfo")
    private MerchantPaymentInfo merchantPaymentInfo;
    private String redirectUrl;
    private String webHookUrl;
}
