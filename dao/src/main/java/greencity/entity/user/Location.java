package greencity.entity.user;

import greencity.entity.coords.Coordinates;
import greencity.entity.user.ubs.OrderAddress;
import greencity.enums.LocationStatus;
import greencity.entity.order.TariffLocation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@EqualsAndHashCode(exclude = {"tariffLocations", "orderAddresses"})
@ToString(exclude = {"tariffLocations", "orderAddresses"})
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
    private List<OrderAddress> orderAddresses = new ArrayList<>();

    /**
     * helper method, that allows to save OrderAddress entity in database correctly.
     *
     * @param orderAddress address of ubs_user for order {@link OrderAddress}
     * @author Safarov Renat
     */
    public void addOrderAddress(OrderAddress orderAddress) {
        orderAddresses.add(orderAddress);
        orderAddress.setLocation(this);
    }
}
