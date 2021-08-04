package greencity.entity.user.employee;

import greencity.entity.order.Order;
import java.util.Set;
import javax.persistence.*;

import lombok.*;
import org.hibernate.annotations.Cascade;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@EqualsAndHashCode
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false)
    private String firstName;

    @Column(length = 30, nullable = false)
    private String lastName;

    @Column(length = 30, nullable = false, unique = true)
    private String phoneNumber;

    @Column(length = 170, unique = true)
    private String email;

    @Column
    private String imagePath;

    @ManyToMany
    @JoinTable(
        name = "employee_position",
        joinColumns = {@JoinColumn(name = "employee_id")},
        inverseJoinColumns = {@JoinColumn(name = "position_id")})
    private Set<Position> employeePosition;

    @ManyToMany
    @JoinTable(
        name = "order_employee",
        joinColumns = {@JoinColumn(name = "employee_id")},
        inverseJoinColumns = {@JoinColumn(name = "order_id")})
    private Set<Order> attachedOrders;

    @ManyToMany
    @JoinTable(
        name = "employee_receiving_station_mapping",
        joinColumns = {@JoinColumn(name = "employee_id")},
        inverseJoinColumns = {@JoinColumn(name = "receiving_station_id")})
    private Set<ReceivingStation> receivingStation;

    @OneToMany(mappedBy = "employee")
    @Cascade(org.hibernate.annotations.CascadeType.DETACH)
    private Set<EmployeeOrderPosition> employeeOrderPositions;
}
