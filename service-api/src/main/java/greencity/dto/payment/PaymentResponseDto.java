package greencity.dto.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentResponseDto {
    @JsonProperty("merchantAccount")
    private String merchantAccount;

    @JsonProperty("orderReference")
    private String orderReference;

    @JsonProperty("merchantSignature")
    private String merchantSignature;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("authCode")
    private String authCode;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("createdDate")
    private String createdDate;

    @JsonProperty("processingDate")
    private String processingDate;

    @JsonProperty("cardPan")
    private String cardPan;

    @JsonProperty("cardType")
    private String cardType;

    @JsonProperty("issuerBankCountry")
    private String issuerBankCountry;

    @JsonProperty("issuerBankName")
    private String issuerBankName;

    @JsonProperty("recToken")
    private String recToken;

    @JsonProperty("transactionStatus")
    private String transactionStatus;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("reasonCode")
    private String reasonCode;

    @JsonProperty("fee")
    private String fee;

    @JsonProperty("paymentSystem")
    private String paymentSystem;

    @JsonProperty("acquirerBankName")
    private String acquirerBankName;
}
