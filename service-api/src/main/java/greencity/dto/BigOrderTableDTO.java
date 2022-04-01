package greencity.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

@Data
@JsonSerialize
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private String region;
    private String settlement;
    private String district;
    private String address;
    private String commentToAddressForClient;
    private String bagsAmount;
    private Long totalOrderSum;
    private String orderCertificateCode;
    private Long generalDiscount;
    private Long amountDue;
    private String commentForOrderByClient;
    private Long totalPayment;
    private String dateOfExport;
    private String timeOfExport;
    private String idOrderFromShop;
    private Long receivingStation;
    private Long responsibleLogicMan;
    private Long responsibleDriver;
    private Long responsibleCaller;
    private Long responsibleNavigator;
    private String commentsForOrder;
    private Boolean isBlocked;
    private String blockedBy;
}
