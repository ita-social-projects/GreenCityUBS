package greencity.entity.order;

import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.ubs.UBSuser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cascade;
import greencity.entity.user.employee.EmployeeOrderPosition;

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

    @Column(columnDefinition = "int default 0")
    private Integer pointsToUse;

    @OneToMany(mappedBy = "order")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<Certificate> certificates;

    @Column(nullable = false, name = "order_status", length = 15)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column
    @Enumerated(EnumType.STRING)
    private OrderPaymentStatus orderPaymentStatus;

    @Column(length = 50)
    private String receivingStation;

    private String note;

    private LocalDateTime deliverFrom;

    private LocalDateTime deliverTo;

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
}
