package greencity.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes(@JsonSubTypes.Type(value = PaymentRequestDto.class, name = "request"))
public class PaymentRequestDto {
    @JsonProperty("merchantAccount")
    private String merchantAccount;
    @JsonProperty("merchantDomainName")
    private String merchantDomainName;
    @JsonProperty("merchantTransactionSecureType")
    private String merchantTransactionSecureType;
    @JsonProperty("orderReference")
    private String orderReference;
    @JsonProperty("orderDate")
    private String orderDate;
    @JsonProperty("amount")
    private Double amount;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("productName")
    private List<String> productName;
    @JsonProperty("productPrice")
    private List<String> productPrice;
    @JsonProperty("productCount")
    private List<String> productCount;
    @JsonProperty("merchantSignature")
    private String signature;
    }
