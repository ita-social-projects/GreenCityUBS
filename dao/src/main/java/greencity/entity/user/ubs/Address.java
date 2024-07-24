package greencity.entity.user.ubs;

import greencity.entity.coords.Coordinates;
import greencity.entity.user.User;
import greencity.enums.AddressStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user"})
@Getter
@Setter
@Builder
@Table(name = "address")
@ToString(exclude = {"user"})
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @Size(min = 1, max = 30, message = "Invalid region name")
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

    @Size(min = 1, max = 10)
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
