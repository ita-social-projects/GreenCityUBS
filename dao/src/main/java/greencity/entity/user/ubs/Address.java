package greencity.entity.user.ubs;

import greencity.entity.coords.Coordinates;
import greencity.enums.AddressStatus;
import greencity.entity.user.User;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "address")
    private List<UBSuser> ubsUsers;

    @ManyToOne
    private User user;

    @Size(min = 1, max = 20, message = "Invalid region name")
    @Column(columnDefinition = "varchar(30)", nullable = false)
    private String region;

    @Size(min = 1, max = 30, message = "Invalid city name")
    @Column(columnDefinition = "varchar(30) default 'Kyiv'", nullable = false)
    private String city;

    @Size(min = 1, max = 50)
    @Column(nullable = false)
    private String street;

    @Size(min = 1, max = 30)
    @Column(nullable = false)
    private String district;

    @Size(min = 1, max = 5)
    @Column(name = "house_number", nullable = false)
    private String houseNumber;

    @Size(max = 5, message = "Invalid house corpus")
    @Column(name = "house_corpus")
    private String houseCorpus;

    @Size(max = 4, message = "Invalid entrance number")
    @Column(name = "entrance_number", nullable = false)
    private String entranceNumber;

    @Column(name = "address_comment", nullable = false)
    private String addressComment;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean actual;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AddressStatus addressStatus;

    @Embedded
    private Coordinates coordinates;

    @Column(nullable = false)
    private String cityEn;

    @Column(nullable = false)
    private String regionEn;

    @Column(nullable = false)
    private String streetEn;

    @Column(nullable = false)
    private String districtEn;
}
