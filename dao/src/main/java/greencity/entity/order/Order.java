package greencity.entity.order;

import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.ReceivingStation;
import greencity.entity.user.ubs.UBSuser;
import greencity.enums.CancellationReason;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.filters.StringListConverter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.AccessLevel;
import org.hibernate.annotations.Cascade;
import org.springframework.util.CollectionUtils;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    "changeOfPointsList", "blockedByEmployee", "certificates", "payment", "employeeOrderPositions",
    "events", "imageReasonNotTakingBags", "additionalOrders"})
@ToString(exclude = {"employeeOrderPositions", "userNotifications", "ubsUser",
    "changeOfPointsList", "blockedByEmployee", "certificates", "payment", "employeeOrderPositions",
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

    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true)
    @Setter(AccessLevel.PRIVATE)
    private List<OrderBag> orderBags = new ArrayList<>();

    /**
     * Updates the list of order bags associated with this order. This method replaces all current items with
     * new ones. It also sets the order reference for each order bag in the new list. This method should be
     * used instead of the default setter to prevent exceptions related to collections with
     * cascade="all-delete-orphan"  when the owning entity instance no longer references it.
     *
     * @param orderBags The new list of order bags to associate with this order.
     */
    public void updateWithNewOrderBags(List<OrderBag> orderBags) {
        if (!CollectionUtils.isEmpty(this.orderBags)) {
            this.orderBags.clear();
        }
        this.orderBags.addAll(orderBags);
        this.orderBags.forEach(ob -> ob.setOrder(this));
    }

    /**
     * Adds an ordered bag to the list of order bags associated with this order.
     * This method adds the specified order bag to the list and sets the order
     * reference for the order bag.
     *
     * @param orderBag The order bag to add to this order.
     */
    public void addOrderedBag(OrderBag orderBag) {
        this.orderBags.add(orderBag);
        orderBag.setOrder(this);
    }

    /**
     * Removes an ordered bag from the list of order bags associated with this
     * order. This method removes the specified order bag from the list.
     *
     * @param orderBag The order bag to remove from this order.
     */
    public void removeOrderedBag(OrderBag orderBag) {
        orderBags.remove(orderBag);
    }
}
