package greencity.dao.entity.user;

import greencity.dao.entity.order.Order;
import greencity.dao.entity.user.ubs.UBSuser;
import greencity.dao.entity.enums.EmailNotification;
import greencity.dao.entity.enums.Role;
import greencity.dao.entity.enums.UserStatus;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(unique = true, nullable = false, length = 50)
    private String email;

    @Enumerated(value = EnumType.ORDINAL)
    @Column(nullable = false)
    private Role role;

    @Enumerated(value = EnumType.ORDINAL)
    private UserStatus userStatus;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "HH:mm")
    private LocalDateTime lastVisit;

    @Column(nullable = false)
    private LocalDateTime dateOfRegistration;

    @OneToOne(mappedBy = "user", cascade = CascadeType.PERSIST)
    private OwnSecurity ownSecurity;

    @OneToOne(mappedBy = "user", cascade = CascadeType.PERSIST)
    private VerifyEmail verifyEmail;

    @OneToOne(mappedBy = "user")
    private RestorePasswordEmail restorePasswordEmail;

    @Enumerated(value = EnumType.ORDINAL)
    private EmailNotification emailNotification;

    @Column(name = "refresh_token_key", nullable = false)
    private String refreshTokenKey;

    @Column(name = "profile_picture")
    private String profilePicturePath;





}
