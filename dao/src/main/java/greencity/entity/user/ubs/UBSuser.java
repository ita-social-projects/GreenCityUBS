package greencity.entity.user.ubs;

import greencity.entity.order.Order;
import greencity.entity.user.User;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"orders", "address"})
@Getter
@Setter
@Builder
@Table(name = "ubs_user")
@Entity
public class UBSuser {
    @ManyToOne
    private Address address;

    @ManyToOne
    @JoinColumn(name = "users_id")
    private User user;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "ubsUser")
    private List<Order> orders;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false)
    private String firstName;

    @Column(length = 30, nullable = false)
    private String lastName;

    @Column(length = 9, nullable = false)
    private String phoneNumber;

    @Column(nullable = false, length = 50)
    private String email;
}
