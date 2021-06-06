package greencity.entity.order;

import greencity.entity.enums.OrderStatus;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.ubs.UBSuser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

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
    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;
}
