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
    private static final Integer TABLE_DEFAULT_COLUMN_WIDTH = 120;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private Employee employee;
    @Column
    private Integer address = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer amountDue = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer bagsAmount = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer blockedBy = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer city = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer clientEmail = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer clientName = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer clientPhone = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer commentForOrderByClient = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer commentToAddressForClient = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer commentsForOrder = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer dateOfExport = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer district = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer generalDiscount = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer orderId = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer idOrderFromShop = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer orderCertificateCode = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer orderDate = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer orderPaymentStatus = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer orderStatus = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer paymentDate = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer receivingStatus = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer region = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer responsibleCaller = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer responsibleDriver = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer responsibleLogicMan = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer responsibleNavigator = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer senderEmail = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer senderName = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer senderPhone = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer timeOfExport = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer totalOrderSum = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer totalPayment = TABLE_DEFAULT_COLUMN_WIDTH;
    @Column
    private Integer violationsAmount = TABLE_DEFAULT_COLUMN_WIDTH;

    /**
     * Constructor with default width for all columns.
     *
     * @param employee who is trying to get columns width
     */
    public TableColumnWidthForEmployee(Employee employee) {
        this.employee = employee;
    }
}
