package greencity.entity.order;

import greencity.enums.LocationStatus;
import greencity.entity.user.Location;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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