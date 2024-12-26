package greencity.entity.user;

import greencity.entity.user.locations.BaseEntityForEnAndUkNames;
import greencity.entity.user.locations.City;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.List;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Table(name = "regions")
@EqualsAndHashCode(exclude = {"locations"})
@ToString(exclude = {"locations", "cities"})
@Entity
public class Region extends BaseEntityForEnAndUkNames {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "region")
    private List<Location> locations;

    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "region", fetch = FetchType.LAZY)
    private List<City> cities;
}
