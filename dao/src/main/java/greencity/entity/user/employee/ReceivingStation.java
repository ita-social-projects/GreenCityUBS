package greencity.entity.user.employee;

import greencity.entity.order.Order;
import greencity.entity.order.TariffsInfo;
import greencity.enums.StationStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@EqualsAndHashCode(exclude = {"tariffsInfo", "createdBy"})
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
