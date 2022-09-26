package greencity.entity.user;

import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Order;
import greencity.entity.order.TariffsInfo;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import greencity.entity.viber.ViberBot;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "users")
@EqualsAndHashCode(exclude = {"ubsUsers", "orders", "addresses", "changeOfPointsList", "telegramBot", "viberBot"})
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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<ChangeOfPoints> changeOfPointsList;

    @Column
    private Integer violations;

    @Column(nullable = false, columnDefinition = "varchar(60)")
    private String uuid;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private TelegramBot telegramBot;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private ViberBot viberBot;

    @Column(name = "date_of_registration")
    private LocalDate dateOfRegistration;

    @OneToMany(mappedBy = "creator")
    private List<TariffsInfo> tariffsInfos;
}
