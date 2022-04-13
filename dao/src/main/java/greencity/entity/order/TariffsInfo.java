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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "tariffs_info")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"services", "services", "bags", "receivingStations", "locations", "orders"})
@EqualsAndHashCode(exclude = {"services", "services", "bags", "receivingStations", "locations", "orders"})
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

    @OneToOne(mappedBy = "tariffsInfo")
    private ReceivingStation receivingStations;

    @Column
    @Enumerated(EnumType.STRING)
    private LocationStatus locationStatus;

    @ManyToOne
    private User creator;

    @Column(nullable = false)
    private LocalDate createdAt;

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
    Courier courier;

    @OneToMany
    List<Location> locations;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "tariffsInfo")
    List<Order> orders;
}
