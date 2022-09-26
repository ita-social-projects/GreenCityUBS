package greencity.entity.user;

import greencity.entity.coords.Coordinates;
import greencity.enums.LocationStatus;
import greencity.entity.order.Bag;
import greencity.entity.order.TariffLocation;
import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@EqualsAndHashCode(exclude = {"bags", "tariffLocations"})
@ToString(exclude = {"bags", "tariffLocations"})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_status")
    @Enumerated(EnumType.STRING)
    private LocationStatus locationStatus;

    @Column(name = "name_uk")
    private String nameUk;

    @Column(name = "name_en")
    private String nameEn;

    @Embedded
    private Coordinates coordinates;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location", fetch = FetchType.LAZY)
    private List<Bag> bags;

    @ManyToOne
    private Region region;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private Set<TariffLocation> tariffLocations;
}
