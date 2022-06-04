package greencity.entity.order;

import greencity.entity.enums.LocationStatus;
import greencity.entity.user.Location;
import lombok.*;

import javax.persistence.*;

@Builder
@Entity
@Table(name = "tariffs_locations")
@EqualsAndHashCode(exclude = {"id", "locationStatus"})
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TariffLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TariffsInfo tariffsInfo;

    @ManyToOne
    private Location location;

    @Column
    @Enumerated(EnumType.STRING)
    private LocationStatus locationStatus;


}