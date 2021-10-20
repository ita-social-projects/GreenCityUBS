package greencity.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class StatusResponseDtoLiqPay {
    private Long acqId;
    private String action;
    private Long agentCommission;
    private Long amount;
    private Long amountBonus;
    private Long amountCredit;
    private Long amountDebit;
    private String authcodeCredit;
    private String authcodeDebit;
    private Long bonusProcent;
    private String bonusType;
    private String cardToken;
    private Long commissionCredit;
    private Long commissionDebit;
    private String createDate;
    private String currency;
    private String currencyCredit;
    private String currencyDebit;
    private String description;
    private String endDate;
    private String info;
    private String ip;
    private Boolean is3ds;
    private String liqpayOrderId;
    private String momentPart;
    private Long mpiEci;
    private Long orderId;
    private Long paymentId;
    private String paytype;
    private String publicKey;
    private Long receiverCommission;
    private String rrnCredit;
    private String rrnDebit;
    private Long senderBonus;
    private String senderCardBank;
    private String senderCardCountry;
    private String senderCardMask2;
    private String senderCardType;
    private Long senderCommission;
    private String senderPhone;
    private String status;
    private Long transactionId;
    private String type;
    private Long version;
}
