package greencity.entity.user;

import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Order;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import greencity.enums.UserStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "users")
@EqualsAndHashCode(exclude = {"ubsUsers", "orders", "addresses", "changeOfPointsList", "telegramBot"})
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<UBSuser> ubsUsers;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<Order> orders;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<Address> addresses;

    @Column(name = "current_points", columnDefinition = "int default 0")
    private Integer currentPoints;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_surname")
    private String recipientSurname;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(name = "alternate_email")
    private String alternateEmail;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Column(name = "user_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserStatus status;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<ChangeOfPoints> changeOfPointsList;

    @Column
    private Integer violations;

    @Column(nullable = false, columnDefinition = "varchar(60)")
    private String uuid;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private TelegramChat telegramBot;

    @Column(name = "date_of_registration")
    private LocalDate dateOfRegistration;

    @Column(name = "chat_link")
    private String chatLink;
}
