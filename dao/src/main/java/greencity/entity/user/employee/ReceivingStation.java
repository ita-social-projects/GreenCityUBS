package greencity.entity.user.employee;

import greencity.entity.order.Order;
import greencity.entity.order.TariffsInfo;
import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@EqualsAndHashCode(exclude = {"employees"})
@Table(name = "receiving_stations")
public class ReceivingStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    @OneToOne
    private TariffsInfo tariffsInfo;

    @ManyToMany(mappedBy = "receivingStation")
    private Set<Employee> employees;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "receivingStation")
    private List<Order> orders;
}
