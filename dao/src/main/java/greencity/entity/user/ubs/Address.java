package greencity.entity.user.ubs;

import greencity.entity.coords.Coordinates;
import greencity.entity.enums.AddressStatus;
import greencity.entity.user.User;
import java.util.List;
import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"ubsUsers", "user"})
@Getter
@Setter
@Builder
@Table(name = "address")
@ToString(exclude = {"ubsUsers", "user"})
public class Address {
    @OneToMany(mappedBy = "address")
    private List<UBSuser> ubsUsers;

    @ManyToOne
    private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "varchar(12) default 'Kyiv'", nullable = false, length = 20)
    private String city;

    @Column(nullable = false, length = 50)
    private String street;

    @Column(nullable = false, length = 30)
    private String district;

    @Column(name = "house_number", nullable = false, length = 5)
    private String houseNumber;

    @Column(length = 5)
    private String houseCorpus;

    @Column(name = "entrance_number", nullable = false, length = 4)
    private String entranceNumber;

    @Column
    private String comment;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean actual;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AddressStatus addressStatus;

    @Embedded
    private Coordinates coordinates;
}
