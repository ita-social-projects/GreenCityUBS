package greencity.entity.user;

import greencity.entity.coords.Coordinates;
import greencity.entity.user.ubs.OrderAddress;
import greencity.enums.LocationStatus;
import greencity.entity.order.TariffLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;
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

    @OneToMany(
        mappedBy = "location",
        cascade = CascadeType.ALL,
        orphanRemoval = true)
    private List<OrderAddress> orderAddresses;

    public void addOrderAddress(OrderAddress orderAddress) {
        orderAddresses.add(orderAddress);
        orderAddress.setLocation(this);
    }
}
