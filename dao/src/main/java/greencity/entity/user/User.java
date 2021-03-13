package greencity.entity.user;

import greencity.entity.order.Order;
import greencity.entity.user.ubs.UBSuser;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "users")
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private Set<UBSuser> ubsUsers;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private List<Order> orders;

    @Column(columnDefinition = "int default 0")
    private Integer currentPoints;

    @ElementCollection
    @CollectionTable(name = "change_of_points_mapping",
        joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "date")
    @Column(name = "amount")
    private Map<LocalDateTime, Integer> changeOfPoints;

    @Column(columnDefinition = "int default 0")
    private Integer violations;

    @Column(nullable = false, columnDefinition = "varchar(60)")
    private String uuid;

    public User(Integer currentPoints, Integer violations, String uuid) {
        this.currentPoints = currentPoints;
        this.violations = violations;
        this.uuid = uuid;
    }
}
