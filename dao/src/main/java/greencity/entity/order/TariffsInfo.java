package greencity.entity.order;

import greencity.entity.enums.LocationStatus;
import greencity.entity.user.User;
import greencity.entity.user.employee.ReceivingStation;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "tariffs_info")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"services", "services", "bags", "receivingStations", "courierLocations"})
@EqualsAndHashCode(exclude = {"services", "services", "bags", "receivingStations", "courierLocations"})
public class TariffsInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tariffsInfo")
    private List<CourierLocation> courierLocations;

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
}
