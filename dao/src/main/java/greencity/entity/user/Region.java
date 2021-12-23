package greencity.entity.user;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "regions")
@EqualsAndHashCode(exclude = {"locationList", "regionTranslation"})
@ToString(exclude = {"locationList", "regionTranslation"})
@Entity
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "region")
    private List<Location> locationList;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "region")
    private List<RegionTranslation> regionTranslation;
}
