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

    /**
     * Constructor with default width for all columns.
     * 
     * @param employee           who is trying to get columns width
     * @param defaultColumnWidth default width
     */
    public TableColumnWidthForEmployee(Employee employee, Integer defaultColumnWidth) {
        this.employee = employee;
        this.address = defaultColumnWidth;
        this.amountDue = defaultColumnWidth;
        this.bagsAmount = defaultColumnWidth;
        this.blockedBy = defaultColumnWidth;
        this.city = defaultColumnWidth;
        this.clientEmail = defaultColumnWidth;
        this.clientName = defaultColumnWidth;
        this.clientPhone = defaultColumnWidth;
        this.commentForOrderByClient = defaultColumnWidth;
        this.commentToAddressForClient = defaultColumnWidth;
        this.commentsForOrder = defaultColumnWidth;
        this.dateOfExport = defaultColumnWidth;
        this.district = defaultColumnWidth;
        this.generalDiscount = defaultColumnWidth;
        this.orderId = defaultColumnWidth;
        this.idOrderFromShop = defaultColumnWidth;
        this.orderCertificateCode = defaultColumnWidth;
        this.orderDate = defaultColumnWidth;
        this.orderPaymentStatus = defaultColumnWidth;
        this.orderStatus = defaultColumnWidth;
        this.paymentDate = defaultColumnWidth;
        this.receivingStatus = defaultColumnWidth;
        this.region = defaultColumnWidth;
        this.responsibleCaller = defaultColumnWidth;
        this.responsibleDriver = defaultColumnWidth;
        this.responsibleLogicMan = defaultColumnWidth;
        this.responsibleNavigator = defaultColumnWidth;
        this.senderEmail = defaultColumnWidth;
        this.senderName = defaultColumnWidth;
        this.senderPhone = defaultColumnWidth;
        this.timeOfExport = defaultColumnWidth;
        this.totalOrderSum = defaultColumnWidth;
        this.totalPayment = defaultColumnWidth;
        this.violationsAmount = defaultColumnWidth;
    }
}
