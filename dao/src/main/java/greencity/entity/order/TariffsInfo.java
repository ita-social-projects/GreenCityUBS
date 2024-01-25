package greencity.entity.order;

import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.ReceivingStation;
import greencity.enums.CourierLimit;
import greencity.enums.TariffStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
@ToString(exclude = {"service", "bags", "receivingStationList", "tariffLocations", "orders", "employees", "creator"})
@EqualsAndHashCode(exclude = {"service", "bags", "receivingStationList", "tariffLocations", "orders", "employees",
    "courier", "creator"})

public class TariffsInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "tariffsInfo")
    private Service service;

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
    private TariffStatus tariffStatus;

    @ManyToOne
    private Employee creator;

    @Column(nullable = false)
    private LocalDate createdAt;

    @Column(name = "min")
    private Long min;

    @Column(name = "max")
    private Long max;

    @Column(name = "limit_description")
    private String limitDescription;

    @Column(name = "courier_limits")
    @Enumerated(EnumType.STRING)
    private CourierLimit courierLimit;

    @ManyToOne
    private Courier courier;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tariffsInfo")
    private Set<TariffLocation> tariffLocations;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tariffsInfo")
    private List<Order> orders;
}
