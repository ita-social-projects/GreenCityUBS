package greencity.dao.entity.user.ubs;

import greencity.dao.entity.order.Order;
import greencity.dao.entity.user.User;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"order"})
@Getter
@Setter
@Builder
@Table(name = "ubs_user")
@Entity
public class UBSuser {
    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address userAddress;

    @ManyToOne
    @JoinColumn(name = "users_id")
    private User user;

    @OneToMany(mappedBy = "ubs_user")
    private List<Order> orders;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 12, nullable = false)
    private String firstName;

    @Column(length = 18, nullable = false)
    private String lastName;

    @Column(length = 9, nullable = false)
    private String phoneNumber;

    @Column(nullable = false, length = 50)
    private String email;
}
