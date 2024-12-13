package greencity.entity.order;

import jakarta.persistence.Column;
import java.util.Objects;
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
    private String commentToAddressForClient;
    @Column(name = "mixed_waste_120")
    private Long mixedWaste120;
    @Column(name = "textile_waste_60")
    private Long textileWaste60;
    @Column(name = "textile_waste_20")
    private Long textileWaste20;
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
    private Long regionId;
    private Long cityId;
    private Long districtId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BigOrderTableViews that = (BigOrderTableViews) o;
        return Objects.equals(id, that.id)
            && Objects.equals(orderStatus, that.orderStatus)
            && Objects.equals(orderPaymentStatus, that.orderPaymentStatus)
            && Objects.equals(orderDate, that.orderDate)
            && Objects.equals(paymentDate, that.paymentDate)
            && Objects.equals(clientName, that.clientName)
            && Objects.equals(clientPhoneNumber, that.clientPhoneNumber)
            && Objects.equals(clientEmail, that.clientEmail)
            && Objects.equals(senderName, that.senderName)
            && Objects.equals(senderPhone, that.senderPhone)
            && Objects.equals(senderEmail, that.senderEmail)
            && Objects.equals(violationsAmount, that.violationsAmount)
            && Objects.equals(region, that.region)
            && Objects.equals(city, that.city)
            && Objects.equals(district, that.district)
            && Objects.equals(address, that.address)
            && Objects.equals(regionEn, that.regionEn)
            && Objects.equals(cityEn, that.cityEn)
            && Objects.equals(districtEn, that.districtEn)
            && Objects.equals(addressEn, that.addressEn)
            && Objects.equals(commentToAddressForClient, that.commentToAddressForClient)
            && Objects.equals(mixedWaste120 == null ? 0 : mixedWaste120,
                that.mixedWaste120 == null ? 0 : that.mixedWaste120)
            && Objects.equals(textileWaste60 == null ? 0 : textileWaste60,
                that.textileWaste60 == null ? 0 : that.textileWaste60)
            && Objects.equals(textileWaste20 == null ? 0 : textileWaste20,
                that.textileWaste20 == null ? 0 : that.textileWaste20)
            && Objects.equals(totalOrderSum, that.totalOrderSum)
            && Objects.equals(orderCertificateCode, that.orderCertificateCode)
            && Objects.equals(generalDiscount, that.generalDiscount)
            && Objects.equals(amountDue, that.amountDue)
            && Objects.equals(commentForOrderByClient, that.commentForOrderByClient)
            && Objects.equals(commentForOrderByAdmin, that.commentForOrderByAdmin)
            && Objects.equals(totalPayment, that.totalPayment)
            && Objects.equals(dateOfExport, that.dateOfExport)
            && Objects.equals(timeOfExport, that.timeOfExport)
            && Objects.equals(idOrderFromShop, that.idOrderFromShop)
            && Objects.equals(receivingStation, that.receivingStation)
            && Objects.equals(receivingStationId, that.receivingStationId)
            && Objects.equals(responsibleLogicMan, that.responsibleLogicMan)
            && Objects.equals(responsibleLogicManId, that.responsibleLogicManId)
            && Objects.equals(responsibleDriver, that.responsibleDriver)
            && Objects.equals(responsibleDriverId, that.responsibleDriverId)
            && Objects.equals(responsibleCaller, that.responsibleCaller)
            && Objects.equals(responsibleCallerId, that.responsibleCallerId)
            && Objects.equals(responsibleNavigator, that.responsibleNavigator)
            && Objects.equals(responsibleNavigatorId, that.responsibleNavigatorId)
            && Objects.equals(isBlocked, that.isBlocked)
            && Objects.equals(blockedBy, that.blockedBy)
            && Objects.equals(tariffsInfoId, that.tariffsInfoId)
            && Objects.equals(regionId, that.regionId)
            && Objects.equals(cityId, that.cityId)
            && Objects.equals(districtId, that.districtId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderStatus, orderPaymentStatus, orderDate, paymentDate, clientName, clientPhoneNumber,
            clientEmail, senderName, senderPhone, senderEmail, violationsAmount, region, city, district, address,
            regionEn,
            cityEn, districtEn, addressEn, commentToAddressForClient, mixedWaste120, textileWaste60, textileWaste20,
            totalOrderSum, orderCertificateCode, generalDiscount, amountDue, commentForOrderByClient,
            commentForOrderByAdmin, totalPayment, dateOfExport, timeOfExport, idOrderFromShop, receivingStation,
            receivingStationId, responsibleLogicMan, responsibleLogicManId, responsibleDriver, responsibleDriverId,
            responsibleCaller, responsibleCallerId, responsibleNavigator, responsibleNavigatorId, isBlocked, blockedBy,
            tariffsInfoId, regionId, cityId, districtId);
    }
}
