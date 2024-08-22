package greencity.entity.user;

import greencity.entity.user.locations.City;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "regions")
@EqualsAndHashCode(exclude = {"locations"})
@ToString(exclude = {"locations", "cities"})
@Entity
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "region")
    private List<Location> locations;

    @Column(name = "name_uk")
    private String ukrName;

    @Column(name = "name_en")
    private String enName;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "region", fetch = FetchType.LAZY)
    private List<City> cities;
}
