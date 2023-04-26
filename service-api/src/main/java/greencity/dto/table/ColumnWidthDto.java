package greencity.dto.table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ColumnWidthDto {
    private Integer address;
    private Integer amountDue;
    private Integer bagsAmount;
    private Integer blockedBy;
    private Integer city;
    private Integer clientEmail;
    private Integer clientName;
    private Integer clientPhone;
    private Integer commentForOrderByClient;
    private Integer commentToAddressForClient;
    private Integer commentsForOrder;
    private Integer dateOfExport;
    private Integer district;
    private Integer generalDiscount;
    private Integer id;
    private Integer idOrderFromShop;
    private Integer orderCertificateCode;
    private Integer orderDate;
    private Integer orderPaymentStatus;
    private Integer orderStatus;
    private Integer paymentDate;
    private Integer receivingStatus;
    private Integer region;
    private Integer responsibleCaller;
    private Integer responsibleDriver;
    private Integer responsibleLogicMan;
    private Integer responsibleNavigator;
    private Integer senderEmail;
    private Integer senderName;
    private Integer senderPhone;
    private Integer timeOfExport;
    private Integer totalOrderSum;
    private Integer totalPayment;
    private Integer violationsAmount;
}
