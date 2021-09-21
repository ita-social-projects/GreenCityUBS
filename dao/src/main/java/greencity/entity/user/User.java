package greencity.entity.user;

import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Order;
import greencity.entity.telegram.TelegramBot;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import greencity.entity.viber.ViberBot;
import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "users")
@EqualsAndHashCode(exclude = {"ubsUsers", "orders", "addresses", "changeOfPointsList",
    "violationsDescription", "telegramBot", "viberBot"})
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private Set<UBSuser> ubsUsers;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private List<Order> orders;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private List<Address> addresses;

    @Column(columnDefinition = "int default 0")
    private Integer currentPoints;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column
    private String recipientSurname;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "order")
    private List<ChangeOfPoints> changeOfPointsList;

    @ElementCollection
    @CollectionTable(name = "violations_description_mapping",
        joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "order_id")
    @Column(name = "description")
    private Map<Long, String> violationsDescription;

    @Column
    private Integer violations;

    @Column(nullable = false, columnDefinition = "varchar(60)")
    private String uuid;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private TelegramBot telegramBot;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private ViberBot viberBot;

    @OneToOne(mappedBy = "user")
    private Employee employee;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private List<Violation> violationsList;

    @ManyToOne()
    @JoinColumn(name = "last_order_location", referencedColumnName = "id")
    private Location lastLocation;
}