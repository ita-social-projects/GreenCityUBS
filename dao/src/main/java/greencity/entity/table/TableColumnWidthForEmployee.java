package greencity.entity.table;

import greencity.entity.user.employee.Employee;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "column_width_for_employee")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class TableColumnWidthForEmployee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private Employee employee;
    @Column
    private Integer address;
    @Column
    private Integer amountDue;
    @Column
    private Integer bagsAmount;
    @Column
    private Integer blockedBy;
    @Column
    private Integer city;
    @Column
    private Integer clientEmail;
    @Column
    private Integer clientName;
    @Column
    private Integer clientPhone;
    @Column
    private Integer commentForOrderByClient;
    @Column
    private Integer commentToAddressForClient;
    @Column
    private Integer commentsForOrder;
    @Column
    private Integer dateOfExport;
    @Column
    private Integer district;
    @Column
    private Integer generalDiscount;
    @Column
    private Integer orderId;
    @Column
    private Integer idOrderFromShop;
    @Column
    private Integer orderCertificateCode;
    @Column
    private Integer orderDate;
    @Column
    private Integer orderPaymentStatus;
    @Column
    private Integer orderStatus;
    @Column
    private Integer paymentDate;
    @Column
    private Integer receivingStatus;
    @Column
    private Integer region;
    @Column
    private Integer responsibleCaller;
    @Column
    private Integer responsibleDriver;
    @Column
    private Integer responsibleLogicMan;
    @Column
    private Integer responsibleNavigator;
    @Column
    private Integer senderEmail;
    @Column
    private Integer senderName;
    @Column
    private Integer senderPhone;
    @Column
    private Integer timeOfExport;
    @Column
    private Integer totalOrderSum;
    @Column
    private Integer totalPayment;
    @Column
    private Integer violationsAmount;
}
