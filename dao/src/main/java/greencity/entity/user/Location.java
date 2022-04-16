package greencity.entity.user;

import greencity.entity.coords.Coordinates;
import greencity.entity.enums.LocationStatus;
import greencity.entity.order.Bag;
import greencity.entity.order.TariffsInfo;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@EqualsAndHashCode(exclude = {"courierLocations", "bags"})
@ToString(exclude = {"courierLocations", "bags"})
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

    @Column(name = "name_Uk")
    private String nameUk;

    @Column(name = "name_En")
    private String nameEn;

    @Embedded
    private Coordinates coordinates;
    /*-
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")
    List<CourierLocation> courierLocations;
    */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location", fetch = FetchType.LAZY)
    List<Bag> bags;
/*-
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location", fetch = FetchType.LAZY)
    List<LocationTranslation> locationTranslations;
*/
    @ManyToOne
    private Region region;

    @ManyToOne
    TariffsInfo tariffsInfo;

}
