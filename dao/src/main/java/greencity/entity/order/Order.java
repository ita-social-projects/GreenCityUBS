package greencity.entity.order;

import greencity.entity.enums.CancellationReason;
import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.ubs.UBSuser;
import greencity.filters.StringListConverter;
import lombok.*;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
@EqualsAndHashCode
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "users_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "ubs_user_id")
    private UBSuser ubsUser;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "order")
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

    @Column(name = "order_payment_status")
    @Enumerated(EnumType.STRING)
    private OrderPaymentStatus orderPaymentStatus;

    @Column(name = "receiving_station", length = 50)
    private String receivingStation;

    private String note;

    @Column(name = "deliver_from")
    private LocalDateTime deliverFrom;

    @Column(name = "deliver_to")
    private LocalDateTime deliverTo;

    @Column(name = "date_of_export")
    private LocalDate dateOfExport;

    @ManyToMany(mappedBy = "attachedOrders")
    private Set<Employee> attachedEmployees;

    @ElementCollection
    @CollectionTable(name = "order_additional", joinColumns = @JoinColumn(name = "orders_id"))
    @Column(name = "additional_order")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<String> additionalOrders;

    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @OneToMany(mappedBy = "order")
    private List<Payment> payment;

    @OneToMany(mappedBy = "order")
    @Cascade(org.hibernate.annotations.CascadeType.DETACH)
    private Set<EmployeeOrderPosition> employeeOrderPositions;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "order")
    private List<Event> events;

    @Column(name = "reason_not_taking_bag_description")
    private String reasonNotTakingBagDescription;

    @Column(name = "image_reason_not_taking_bags")
    @Convert(converter = StringListConverter.class)
    private List<String> imageReasonNotTakingBags;
}
