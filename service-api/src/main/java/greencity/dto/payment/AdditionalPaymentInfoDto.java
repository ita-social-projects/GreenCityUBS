package greencity.dto.payment;

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
public class AdditionalPaymentInfoDto {
    // CHECKSTYLE:OFF
    private String bankName;
    private String bankCountry;
    private String bankResponseCode;
    private String cardProduct;
    private String cardCategory;
    private Double settlementFee;
    private String captureStatus;
    private Double clientFee;
    private String ipaddressV4;
    private Double captureAmount;
    private String cardType;
    private String reservationData;
    private String bankResponseDescription;
    private Integer transactionId;
    private String timeEnd;
    private String cardNumber;
}
