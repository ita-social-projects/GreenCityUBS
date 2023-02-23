package greencity.entity.user;

import greencity.entity.coords.Coordinates;
import greencity.enums.LocationStatus;
import greencity.entity.order.TariffLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Entity
@EqualsAndHashCode(exclude = {"tariffLocations"})
@ToString(exclude = {"tariffLocations"})
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

    @ManyToOne
    private Region region;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private Set<TariffLocation> tariffLocations;
}
