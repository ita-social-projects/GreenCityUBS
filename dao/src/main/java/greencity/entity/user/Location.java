package greencity.entity.user;

import greencity.entity.coords.Coordinates;
import greencity.entity.enums.LocationStatus;
import greencity.entity.order.Bag;
import greencity.entity.order.TariffsInfo;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@EqualsAndHashCode(exclude = {"bags", "tariffsInfoList"})
@ToString(exclude = {"bags", "tariffsInfoList"})
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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location", fetch = FetchType.LAZY)
    private List<Bag> bags;

    @ManyToOne
    private Region region;

    @ManyToMany(mappedBy = "locations", cascade = CascadeType.ALL)
    private List<TariffsInfo> tariffsInfoList;
}
