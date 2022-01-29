package greencity.entity.order;

import greencity.entity.enums.LocationStatus;
import greencity.entity.user.Region;
import greencity.entity.user.employee.ReceivingStation;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table("tariffs_info")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class TariffsInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToMany
    List<CourierLocation> courierLocationList;

    @OneToMany
    List<Region> regions;

    @OneToOne
    ReceivingStation receivingStation;

    @Column
    @Enumerated(EnumType.STRING)
    LocationStatus locationStatus;
}
