package greencity.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestLiqPayDto {
    @JsonProperty("order_id")
    private String orderId;
    private String amount;
    private String currency;
    private String description;
    private String sandbox;
    @JsonProperty("result_url")
    private String resultUrl;
}
