package greencity.entity.order;

import greencity.entity.enums.CourierLimit;
import greencity.entity.enums.LocationStatus;
import greencity.entity.user.Location;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(exclude = {"courier", "orders", "location"})
@ToString(exclude = {"courier", "orders", "location"})
@Table(name = "courier_locations")
public class CourierLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long minAmountOfBigBags;

    @Column
    private Long maxAmountOfBigBags;

    @Column
    private Long minPriceOfOrder;

    @Column
    private Long maxPriceOfOrder;

    @Column(name = "courier_limits")
    @Enumerated(EnumType.STRING)
    private CourierLimit courierLimit;

    @ManyToOne
    Location location;

    @ManyToOne
    Courier courier;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "courierLocations")
    List<Order> orders;

    @ManyToOne
    TariffsInfo tariffsInfo;

    @Column
    @Enumerated(EnumType.STRING)
    LocationStatus locationStatus;
}
