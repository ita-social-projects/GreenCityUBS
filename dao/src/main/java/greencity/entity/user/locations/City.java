package greencity.entity.user.locations;

import greencity.entity.user.Region;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString(exclude = "districts")
@EqualsAndHashCode(exclude = "districts")
@Table(name = "cities")
public class City extends BaseEntityForEnAndUkNames {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "city", fetch = FetchType.LAZY)
    private List<District> districts;
}
