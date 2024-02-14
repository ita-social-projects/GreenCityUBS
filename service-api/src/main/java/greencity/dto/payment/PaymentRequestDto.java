package greencity.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes(@JsonSubTypes.Type(value = PaymentRequestDto.class, name = "request"))
public class PaymentRequestDto {
    @JsonProperty("order_id")
    private String orderId;
    @JsonProperty("merchant_id")
    private Integer merchantId;
    @JsonProperty("order_desc")
    private String orderDescription;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("amount")
    private Long amount;
    @JsonProperty("signature")
    private String signature;
    @JsonProperty("response_url")
    private String responseUrl;
}
