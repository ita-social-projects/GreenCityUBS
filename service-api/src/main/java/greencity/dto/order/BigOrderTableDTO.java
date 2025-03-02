package greencity.dto.order;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonSerialize
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
public class BigOrderTableDTO {
    private Long id;
    private String orderStatus;
    private String orderPaymentStatus;
    private String orderDate;
    private String paymentDate;
    private String clientName;
    private String clientPhone;
    private String clientEmail;
    private String senderName;
    private String senderPhone;
    private String senderEmail;
    private Integer violationsAmount;
    private SenderLocation region;
    private SenderLocation city;
    private SenderLocation district;
    private SenderLocation address;
    private String commentToAddressForClient;
    private String mixedWaste120L;
    private String textileWaste60L;
    private String textileWaste20L;
    private OtherPackages otherPackages;
    private Double totalOrderSum;
    private String orderCertificateCode;
    private Long generalDiscount;
    private Double amountDue;
    private String commentForOrderByClient;
    private Double totalPayment;
    private String dateOfExport;
    private String timeOfExport;
    private String idOrderFromShop;
    private String receivingStation;
    private String responsibleLogicMan;
    private String responsibleDriver;
    private String responsibleCaller;
    private String responsibleNavigator;
    private String commentsForOrder;
    private Boolean isBlocked;
    private String blockedBy;
}
