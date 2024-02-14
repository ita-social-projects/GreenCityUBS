package greencity.entity.table;

import greencity.entity.user.employee.Employee;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;

@Entity
@Table(name = "column_width_for_employee")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableColumnWidthForEmployee {
    private static final Integer DEFAULT_WIDTH = 120;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private Employee employee;
    @Column
    private Integer address = DEFAULT_WIDTH;
    @Column
    private Integer amountDue = DEFAULT_WIDTH;
    @Column
    private Integer bagsAmount = DEFAULT_WIDTH;
    @Column
    private Integer blockedBy = DEFAULT_WIDTH;
    @Column
    private Integer city = DEFAULT_WIDTH;
    @Column
    private Integer clientEmail = DEFAULT_WIDTH;
    @Column
    private Integer clientName = DEFAULT_WIDTH;
    @Column
    private Integer clientPhone = DEFAULT_WIDTH;
    @Column
    private Integer commentForOrderByClient = DEFAULT_WIDTH;
    @Column
    private Integer commentToAddressForClient = DEFAULT_WIDTH;
    @Column
    private Integer commentsForOrder = DEFAULT_WIDTH;
    @Column
    private Integer dateOfExport = DEFAULT_WIDTH;
    @Column
    private Integer district = DEFAULT_WIDTH;
    @Column
    private Integer generalDiscount = DEFAULT_WIDTH;
    @Column
    private Integer orderId = DEFAULT_WIDTH;
    @Column
    private Integer idOrderFromShop = DEFAULT_WIDTH;
    @Column
    private Integer orderCertificateCode = DEFAULT_WIDTH;
    @Column
    private Integer orderDate = DEFAULT_WIDTH;
    @Column
    private Integer orderPaymentStatus = DEFAULT_WIDTH;
    @Column
    private Integer orderStatus = DEFAULT_WIDTH;
    @Column
    private Integer paymentDate = DEFAULT_WIDTH;
    @Column
    private Integer receivingStatus = DEFAULT_WIDTH;
    @Column
    private Integer region = DEFAULT_WIDTH;
    @Column
    private Integer responsibleCaller = DEFAULT_WIDTH;
    @Column
    private Integer responsibleDriver = DEFAULT_WIDTH;
    @Column
    private Integer responsibleLogicMan = DEFAULT_WIDTH;
    @Column
    private Integer responsibleNavigator = DEFAULT_WIDTH;
    @Column
    private Integer senderEmail = DEFAULT_WIDTH;
    @Column
    private Integer senderName = DEFAULT_WIDTH;
    @Column
    private Integer senderPhone = DEFAULT_WIDTH;
    @Column
    private Integer timeOfExport = DEFAULT_WIDTH;
    @Column
    private Integer totalOrderSum = DEFAULT_WIDTH;
    @Column
    private Integer totalPayment = DEFAULT_WIDTH;
    @Column
    private Integer violationsAmount = DEFAULT_WIDTH;

    /**
     * Constructor with default width for all columns.
     *
     * @param employee who is trying to get columns width
     */
    public TableColumnWidthForEmployee(Employee employee) {
        this.employee = employee;
    }
}
