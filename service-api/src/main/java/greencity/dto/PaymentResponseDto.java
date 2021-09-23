package greencity.dto;

import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PaymentResponseDto {
    // CHECKSTYLE:OFF
    @Length(max = 1024)
    private String orderId;
    private Integer merchantId;
    private Integer amount;
    @Length(max = 3)
    private String currency;
    @Length(max = 50)
    private String orderStatus;
    @Length(max = 50)
    private String responseStatus;
    @Length(max = 40)
    private String signature;
    @Length(max = 50)
    private String tranType;
    @Length(max = 16)
    private String senderCellPhone;
    @Length(max = 50)
    private String senderAccount;
    @Length(max = 19)
    private String maskedCard;
    private Integer cardBin;
    @Length(max = 50)
    private String cardType;
    @Length(max = 50)
    private String rrn;
    @Length(max = 6)
    private String approvalCode;
    private Integer responseCode;
    @Length(max = 1024)
    private String responseDescription;
    private Integer reversalAmount;
    private Integer settlementAmount;
    @Length(max = 3)
    private String settlementCurrency;
    @Length(max = 19)
    private String orderTime;
    @Length(max = 10)
    private String settlementDate;
    private Integer eci;
    private Integer fee;
    @Length(max = 50)
    private String paymentSystem;
    @Length(max = 254)
    private String senderEmail;
    private Integer paymentId;
    private Integer actualAmount;
    @Length(max = 3)
    private String actualCurrency;
    @Length(max = 1024)
    private String productId;
    @Length(max = 2048)
    private String merchantData;
    @Length(max = 50)
    private String verificationStatus;
    @Length(max = 40)
    private String rectoken;
    @Length(max = 19)
    private String rectokenLifetime;
    private AdditionalPaymentInfoDto additionalInfo;
}
