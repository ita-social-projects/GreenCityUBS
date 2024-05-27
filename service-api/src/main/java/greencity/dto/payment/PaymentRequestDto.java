package greencity.dto.payment;

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
    @JsonProperty("merchantDomainName")
    private String merchantDomainName;
    @JsonProperty("orderReference")
    private String orderReference;
    @JsonProperty("orderDate")
    private String orderDate;
    @JsonProperty("amount")
    private Double amount;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("productName")
    private String[] productName;
    @JsonProperty("productCount")
    private String[] productCount;
    @JsonProperty("productPrice")
    private String[] productPrice;
    @JsonProperty("merchantSignature")
    private String signature;
    }
