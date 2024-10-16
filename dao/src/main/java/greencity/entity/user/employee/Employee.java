package greencity.entity.user.employee;

import greencity.entity.TariffsInfoRecievingEmployee;
import greencity.entity.order.Service;
import greencity.entity.table.TableColumnWidthForEmployee;
import greencity.enums.EmployeeStatus;
import greencity.entity.order.Order;
import greencity.entity.order.TariffsInfo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@EqualsAndHashCode(exclude = {"employeePosition", "employeeOrderPositions", "orders", "tariffs",
    "createdServices", "editedServices"})
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", length = 30, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 30, nullable = false)
    private String lastName;

    @Column(name = "phone_number", length = 30, nullable = false, unique = true)
    private String phoneNumber;

    @Column(length = 170, unique = true)
    private String email;

    @Column(nullable = false, columnDefinition = "varchar(60)")
    private String uuid;

    @Column(name = "image_path")
    private String imagePath;

    @Column(nullable = false, name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private EmployeeStatus employeeStatus;

    @ManyToMany
    @JoinTable(
        name = "employee_position",
        joinColumns = {@JoinColumn(name = "employee_id")},
        inverseJoinColumns = {@JoinColumn(name = "position_id")})
    private Set<Position> employeePosition;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TariffsInfoRecievingEmployee> tariffsInfoReceivingEmployees;

    @OneToMany(mappedBy = "creator")
    private List<TariffsInfo> tariffs;

    @OneToMany(mappedBy = "employee")
    @Cascade(org.hibernate.annotations.CascadeType.DETACH)
    private Set<EmployeeOrderPosition> employeeOrderPositions;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "blockedByEmployee")
    private Set<Order> orders;

    @OneToMany(mappedBy = "createdBy")
    private Set<Service> createdServices;

    @OneToMany(mappedBy = "editedBy")
    private Set<Service> editedServices;

    @OneToOne(mappedBy = "employee")
    private TableColumnWidthForEmployee tableColumnWidthForEmployee;
}
