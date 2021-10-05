package greencity.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;

@Builder
@JsonSerialize
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BigOrderTableDTO {
    private Long id;
    private String orderStatus;
    private String paymentStatus;
    private String orderDate;
    private String paymentDate;
    private String clientName;
    private String phoneNumber;
    private String email;
    private String senderName;
    private String senderPhone;
    private String senderEmail;
    private Integer violationsAmount;
    private String location;
    private String district;
    private String address;
    private String commentToAddressForClient;
    private Integer bagsAmount;
    private Long totalOrderSum;
    private String orderCertificateCode;
    private String orderCertificatePoints;
    private Long amountDue;
    private String commentForOrderByClient;
    private String payment;
    private String dateOfExport;
    private String timeOfExport;
    private String idOrderFromShop;
    private String receivingStation;
    private String responsibleManager;
    private String responsibleLogicMan;
    private String responsibleDriver;
    private String responsibleNavigator;
    private String commentsForOrder;
}
