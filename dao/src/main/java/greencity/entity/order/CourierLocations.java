package greencity.entity.order;

import greencity.entity.enums.CourierLimit;
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
@Table(name = "courier_locations")
public class CourierLocations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_amount_of_big_bags")
    private Long minAmountOfBigBags;

    @Column(name = "max_amount_of_big_bags")
    private Long maxAmountOfBigBags;

    @Column(name = "min_price_of_order")
    private Long minPriceOfOrder;

    @Column(name = "max_price_of_order")
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
}
