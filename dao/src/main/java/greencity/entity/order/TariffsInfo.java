package greencity.entity.order;

import greencity.entity.enums.CourierLimit;
import greencity.entity.enums.LocationStatus;
import greencity.entity.user.Location;
import greencity.entity.user.User;
import greencity.entity.user.employee.ReceivingStation;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tariffs_info")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"services", "services", "bags", "receivingStationList", "locations", "orders"})
@EqualsAndHashCode(exclude = {"services", "services", "bags", "receivingStationList", "locations", "orders"})
public class TariffsInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*-
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tariffsInfo")
    private List<CourierLocation> courierLocations;
    */

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "tariffsInfo")
    private List<Service> services;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tariffsInfo", fetch = FetchType.LAZY)
    private List<Bag> bags;

    //@OneToOne(mappedBy = "tariffsInfo")
    @ManyToMany
    @JoinTable(name = "station_tariffs",
        joinColumns = @JoinColumn(name = "tariffs_info_id"),
        inverseJoinColumns = @JoinColumn(name = "receiving_station_id"))
    private Set<ReceivingStation> receivingStationList;

    @Column
    @Enumerated(EnumType.STRING)
    private LocationStatus locationStatus;

    @ManyToOne
    private User creator;

    @Column(nullable = false)
    private LocalDate createdAt;

    @Column(name="min_amount_of_big_bags")
    private Long minAmountOfBigBags;

    @Column(name="max_amount_of_big_bags")
    private Long maxAmountOfBigBags;

    @Column(name="min_price_of_order")
    private Long minPriceOfOrder;

    @Column(name="max_price_of_order")
    private Long maxPriceOfOrder;

    @Column(name = "courier_limits")
    @Enumerated(EnumType.STRING)
    private CourierLimit courierLimit;

    @ManyToOne
    Courier courier;

    @ManyToMany
    @JoinTable(name = "location_tariffs",
            joinColumns = @JoinColumn(name = "tariffs_info_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id"))
    Set<Location> locations;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "tariffsInfo")
    List<Order> orders;
}
