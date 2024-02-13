package greencity.entity.user.ubs;

import greencity.entity.order.Order;
import greencity.entity.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
