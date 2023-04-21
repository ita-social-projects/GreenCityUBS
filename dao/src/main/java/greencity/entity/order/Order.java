package greencity.entity.order;

import greencity.enums.CancellationReason;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.ReceivingStation;
import greencity.entity.user.ubs.UBSuser;
import greencity.filters.StringListConverter;
import lombok.*;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "orders")
@EqualsAndHashCode(exclude = {"employeeOrderPositions", "userNotifications", "ubsUser",
    "changeOfPointsList", "blockedByEmployee", "certificates", "attachedEmployees", "payment", "employeeOrderPositions",
    "events", "imageReasonNotTakingBags", "additionalOrders"})
@ToString(exclude = {"employeeOrderPositions", "userNotifications", "ubsUser",
    "changeOfPointsList", "blockedByEmployee", "certificates", "attachedEmployees", "payment", "employeeOrderPositions",
    "events", "imageReasonNotTakingBags", "additionalOrders"})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "order")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private List<UserNotification> userNotifications;

    @ManyToOne
    @JoinColumn(name = "users_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "ubs_user_id")
    private UBSuser ubsUser;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
    private List<ChangeOfPoints> changeOfPointsList;

    private boolean blocked;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee blockedByEmployee;

    @ElementCollection
    @CollectionTable(name = "order_bag_mapping",
        joinColumns = {@JoinColumn(name = "order_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "bag_id")
    @Column(name = "exported_quantity")
    private Map<Integer, Integer> exportedQuantity;
    @ElementCollection
    @CollectionTable(name = "order_bag_mapping",
        joinColumns = {@JoinColumn(name = "order_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "bag_id")
    @Column(name = "confirmed_quantity")
    private Map<Integer, Integer> confirmedQuantity;
    @ElementCollection
    @CollectionTable(name = "order_bag_mapping",
        joinColumns = {@JoinColumn(name = "order_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "bag_id")
    @Column(name = "amount")
    private Map<Integer, Integer> amountOfBagsOrdered;

    @Column
    private String comment;

    @Column(name = "cancellation_comment")
    private String cancellationComment;

    @Column(name = "cancellation_reason")
    @Enumerated(EnumType.STRING)
    private CancellationReason cancellationReason;

    @Column(name = "points_to_use", columnDefinition = "int default 0")
    private Integer pointsToUse;

    @OneToMany(mappedBy = "order")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<Certificate> certificates;

    @Column(nullable = false, name = "order_status", length = 15)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(nullable = false, name = "order_payment_status")
    @Enumerated(EnumType.STRING)
    private OrderPaymentStatus orderPaymentStatus;

    @ManyToOne
    @JoinColumn(name = "receiving_station_id")
    private ReceivingStation receivingStation;

    @Column(name = "deliver_from")
    private LocalDateTime deliverFrom;

    @Column(name = "deliver_to")
    private LocalDateTime deliverTo;

    @Column(name = "date_of_export")
    private LocalDate dateOfExport;

    @ElementCollection
    @CollectionTable(name = "order_additional",
        joinColumns = @JoinColumn(name = "orders_id", referencedColumnName = "id"))
    @Column(name = "additional_order")
    private Set<String> additionalOrders;

    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @OneToMany(mappedBy = "order")
    private List<Payment> payment;

    @OneToMany(mappedBy = "order")
    @Cascade(org.hibernate.annotations.CascadeType.MERGE)
    private Set<EmployeeOrderPosition> employeeOrderPositions;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
    private List<Event> events;

    @Column(name = "reason_not_taking_bag_description")
    private String reasonNotTakingBagDescription;

    @Column(name = "image_reason_not_taking_bags")
    @Convert(converter = StringListConverter.class)
    private List<String> imageReasonNotTakingBags;

    @Column(name = "admin_comment")
    private String adminComment;

    @Column(name = "counter_order_payment_id")
    private Long counterOrderPaymentId;

    @ManyToOne
    private TariffsInfo tariffsInfo;

    @Column(name = "sum_total_amount_without_discounts")
    private Long sumTotalAmountWithoutDiscounts;

    @Column(name = "ubs_courier_sum")
    private Long ubsCourierSum;

    @Column(name = "write_off_station_sum")
    private Long writeOffStationSum;
}
