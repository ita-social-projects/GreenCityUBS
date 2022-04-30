package greencity.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PaymentResponseDto {
    @Length(max = 1024)
    @JsonProperty("order_id")
    private String orderId;
    @JsonProperty("merchant_id")
    private Integer merchantId;
    private Integer amount;
    @Length(max = 3)
    private String currency;
    @Length(max = 50)
    @JsonProperty("order_status")
    private String orderStatus;
    @Length(max = 50)
    @JsonProperty("responseStatus")
    private String responseStatus;
    @Length(max = 40)
    private String signature;
    @Length(max = 50)
    @JsonProperty("tran_type")
    private String tranType;
    @Length(max = 16)
    @JsonProperty("sender_cell_phone")
    private String senderCellPhone;
    @Length(max = 50)
    @JsonProperty("sender_account")
    private String senderAccount;
    @Length(max = 19)
    @JsonProperty("masked_card")
    private String maskedCard;
    @JsonProperty("card_bin")
    private Integer cardBin;
    @Length(max = 50)
    @JsonProperty("card_type")
    private String cardType;
    @Length(max = 50)
    private String rrn;
    @Length(max = 6)
    @JsonProperty("approval_code")
    private String approvalCode;
    @JsonProperty("response_code")
    private Integer responseCode;
    @Length(max = 1024)
    @JsonProperty("response_description")
    private String responseDescription;
    @JsonProperty("reversal_amount")
    private Integer reversalAmount;
    @JsonProperty("settlement_amount")
    private Integer settlementAmount;
    @Length(max = 3)
    @JsonProperty("settlement_currency")
    private String settlementCurrency;
    @Length(max = 19)
    @JsonProperty("order_time")
    private String orderTime;
    @Length(max = 10)
    @JsonProperty("settlement_date")
    private String settlementDate;
    private Integer eci;
    private Integer fee;
    @Length(max = 50)
    @JsonProperty("payment_system")
    private String paymentSystem;
    @Length(max = 254)
    @JsonProperty("sender_email")
    private String senderEmail;
    @JsonProperty("payment_id")
    private Integer paymentId;
    @JsonProperty("actual_amount")
    private Integer actualAmount;
    @Length(max = 3)
    @JsonProperty("actual_currency")
    private String actualCurrency;
    @Length(max = 1024)
    @JsonProperty("product_id")
    private String productId;
    @Length(max = 2048)
    @JsonProperty("merchant_data")
    private String merchantData;
    @Length(max = 50)
    @JsonProperty("verification_status")
    private String verificationStatus;
    @Length(max = 40)
    private String rectoken;
    @Length(max = 19)
    @JsonProperty("rectoken_lifetime")
    private String rectokenLifetime;
    @JsonProperty("parent_order_id")
    private Integer parentOrderId;
}
