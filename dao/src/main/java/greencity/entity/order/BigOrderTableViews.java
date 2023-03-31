package greencity.entity.order;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "big_order_table")
public class BigOrderTableViews {
    @Id
    private Long id;
    private String orderStatus;
    private String orderPaymentStatus;
    private LocalDate orderDate;
    private LocalDate paymentDate;
    private String clientName;
    private String clientPhoneNumber;
    private String clientEmail;
    private String senderName;
    private String senderPhone;
    private String senderEmail;
    private Integer violationsAmount;
    private String region;
    private String city;
    private String district;
    private String address;
    private String regionEn;
    private String cityEn;
    private String districtEn;
    private String addressEn;
    private String commentToAddressForClient;
    private String bagAmount;
    private Long totalOrderSum;
    private String orderCertificateCode;
    private Long generalDiscount;
    private Long amountDue;
    private String commentForOrderByClient;
    private String commentForOrderByAdmin;
    private Long totalPayment;
    private LocalDate dateOfExport;
    private String timeOfExport;
    private String idOrderFromShop;
    private String receivingStation;
    private Long receivingStationId;
    private String responsibleLogicMan;
    private Long responsibleLogicManId;
    private String responsibleDriver;
    private Long responsibleDriverId;
    private String responsibleCaller;
    private Long responsibleCallerId;
    private String responsibleNavigator;
    private Long responsibleNavigatorId;
    private String commentsForOrder;
    private Boolean isBlocked;
    private String blockedBy;
    private Long tariffsInfoId;
}
