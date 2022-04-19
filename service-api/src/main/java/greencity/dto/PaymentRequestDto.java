package greencity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

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
    private Integer amount;
    @JsonProperty("signature")
    private String signature;
    @JsonProperty("response_url")
    private String responseUrl;
}
