package greencity.entity.user.ubs;

import greencity.entity.order.Order;
import greencity.entity.user.User;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"orders", "address", "user",})
@ToString(exclude = {"orders", "address", "user",})
@Getter
@Setter
@Builder
@Table(name = "ubs_user")
@Entity
public class UBSuser {
    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private OrderAddress orderAddress;

    @ManyToOne
    @JoinColumn(name = "users_id")
    private User user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "ubsUser")
    private List<Order> orders;

    @Column(name = "first_name", length = 30, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 30, nullable = false)
    private String lastName;

    @Column(name = "phone_number", length = 15, nullable = false)
    private String phoneNumber;

    @Column(name = "sender_first_name", length = 30)
    private String senderFirstName;

    @Column(name = "sender_last_name", length = 30)
    private String senderLastName;

    @Column(name = "sender_email", length = 50)
    private String senderEmail;

    @Column(name = "sender_phone_number", length = 15)
    private String senderPhoneNumber;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(name = "alternate_email", length = 50)
    private String alternateEmail;
}
