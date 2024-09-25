package greencity.entity.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
    private Long regionId;
    private Long cityId;
    private Long districtId;
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
    private Boolean isBlocked;
    private String blockedBy;
    private Long tariffsInfoId;
}
