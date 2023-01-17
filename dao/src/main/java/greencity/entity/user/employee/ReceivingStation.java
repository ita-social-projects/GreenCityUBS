package greencity.entity.user.employee;

import greencity.entity.order.Order;
import greencity.entity.order.TariffsInfo;
import greencity.enums.StationStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@EqualsAndHashCode(exclude = {"tariffsInfo"})
@Table(name = "receiving_stations")
public class ReceivingStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "receivingStationList", cascade = CascadeType.ALL)
    private Set<TariffsInfo> tariffsInfo;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "receivingStation")
    private List<Order> orders;

    @ManyToOne
    private Employee createdBy;

    private LocalDate createDate;

    @Column(name = "station_status")
    @Enumerated(EnumType.STRING)
    private StationStatus stationStatus;
}
