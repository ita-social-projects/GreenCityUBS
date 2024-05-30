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
public class PaymentRequestDto {
    @JsonProperty("transactionType")
    private String transactionType;
    @JsonProperty("merchantAccount")
    private String merchantAccount;
    @JsonProperty("merchantDomainName")
    private String merchantDomainName;
    @JsonProperty("apiVersion")
    private Integer apiVersion;
    @JsonProperty("serviceUrl")
    private String serviceUrl;
    @JsonProperty("orderReference")
    private String orderReference;
    @JsonProperty("orderDate")
    private Long orderDate;
    @JsonProperty("amount")
    private Integer amount;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("productName")
    private List<String> productName;
    @JsonProperty("productPrice")
    private List<Integer> productPrice;
    @JsonProperty("productCount")
    private List<Integer> productCount;
    @JsonProperty("merchantSignature")
    private String signature;
}
