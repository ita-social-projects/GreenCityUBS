package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PaymentResponseDtoLiqPay {
    private Integer acqId;
    private String action;
    private Integer agentCommission;
    private Integer amount;
    private Integer amountBonus;
    private Integer amountCredit;
    private Integer amountDebit;
    private String authcodeCredit;
    private String authcodeDebit;
    private String bonusProcent;
    private String bonusType;
    private String cardToken;
    private Integer commissionCredit;
    private Integer commissionDebit;
    private String completionDate;
    private String createDate;
    private String currency;
    private String currencyCredit;
    private String currencyDebit;
    private String customer;
    private String description;
    private String endDate;
    private String errCode;
    private String errDescription;
    private String info;
    private String ip;
    private Boolean is3Ds;
    private String liqpayOrderId;
    private Integer mpiEci;
    private String orderId;
    private Integer paymentId;
    private String paytype;
    private String publicKey;
    private Integer receiverCommission;
    private String redirectTo;
    private String refundDateLast;
    private String rrnCredit;
    private String rrnDebit;
    private Integer senderBonus;
    private String senderCardBank;
    private String senderCardCountry;
    private String senderCardMask2;
    private String senderCardType;
    private Integer senderCommission;
    private String senderFirstName;
    private String senderLastName;
    private String senderPhone;
    private String status;
    private String token;
    private String type;
    private Integer version;
    private String errErc;
    private String productCategory;
    private String productDescription;
    private String productName;
    private String productUrl;
    private Integer refundAmount;
    private String verifycode;
}
