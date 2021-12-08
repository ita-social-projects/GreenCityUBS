package greencity.entity.user;

import greencity.entity.enums.LocationStatus;
import greencity.entity.order.Bag;
import greencity.entity.order.CourierLocations;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@EqualsAndHashCode(exclude = {"user", "courierLocations", "bags", "locationTranslations"})
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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "lastLocation")
    private List<User> user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")
    List<CourierLocations> courierLocations;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location", fetch = FetchType.LAZY)
    List<Bag> bags;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location", fetch = FetchType.LAZY)
    List<LocationTranslation> locationTranslations;
}
