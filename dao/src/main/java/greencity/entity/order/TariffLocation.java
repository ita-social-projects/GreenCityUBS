package greencity.entity.order;

import greencity.entity.user.Location;
import greencity.enums.LocationStatus;
import jakarta.persistence.*;
import lombok.*;

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