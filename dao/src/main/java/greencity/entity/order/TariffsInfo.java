package greencity.entity.order;

import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.ReceivingStation;
import greencity.enums.CourierLimit;
import greencity.enums.LocationStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tariffs_info")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"services", "bags", "receivingStationList", "tariffLocations", "orders", "employees", "creator"})
@EqualsAndHashCode(exclude = {"services", "bags", "receivingStationList", "tariffLocations", "orders", "employees",
    "courier", "creator"})
public class TariffsInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tariffsInfo")
    private List<Service> services;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tariffsInfo")
    private List<Bag> bags;

    @ManyToMany
    @JoinTable(name = "tariffs_info_receiving_stations_mapping",
        joinColumns = @JoinColumn(name = "tariffs_info_id"),
        inverseJoinColumns = @JoinColumn(name = "receiving_station_id"))
    private Set<ReceivingStation> receivingStationList;

    @ManyToMany(mappedBy = "tariffInfos")
    private Set<Employee> employees;

    @Column
    @Enumerated(EnumType.STRING)
    private LocationStatus locationStatus;

    @ManyToOne
    private Employee creator;

    @Column(nullable = false)
    private LocalDate createdAt;

    @Column(name = "min_quantity")
    private Long minQuantity;

    @Column(name = "max_quantity")
    private Long maxQuantity;
    @Column(name = "limit_description")
    private String limitDescription;

    @Column(name = "courier_limits")
    @Enumerated(EnumType.STRING)
    private CourierLimit courierLimit;

    @ManyToOne
    private Courier courier;

    @OneToMany(mappedBy = "tariffsInfo", cascade = CascadeType.ALL)
    private Set<TariffLocation> tariffLocations;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tariffsInfo")
    private List<Order> orders;
}
