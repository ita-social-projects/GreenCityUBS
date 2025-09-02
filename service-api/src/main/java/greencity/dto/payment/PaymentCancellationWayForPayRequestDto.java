package greencity.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class PaymentCancellationWayForPayRequestDto {
    @JsonProperty("transactionType")
    private String transactionType;
    @JsonProperty("merchantAccount")
    private String merchantAccount;
    @JsonProperty("apiVersion")
    private Integer apiVersion;
    @JsonProperty("orderReference")
    private String orderReference;
    @JsonProperty("merchantSignature")
    private String signature;
}
