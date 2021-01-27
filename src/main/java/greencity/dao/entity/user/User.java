package greencity.dao.entity.user;

import greencity.dao.entity.order.Order;
import greencity.dao.entity.user.ubs.UBSuser;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

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

    @Column(unique = true, nullable = false, length = 50)
    private String email;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UBSuser> ubs_users;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Order> orders;

    @Column(columnDefinition = "int default 0")
    private Integer currentPoints;

    @ElementCollection
    @CollectionTable(name = "change_of_points_mapping",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName =
                    "id")})
    @MapKeyColumn(name = "date")
    @Column(name = "amount")
    private Map<LocalDateTime, Integer> changeOfPoints;
}
