package greencity.entity.user.locations;

import greencity.entity.user.Region;
import jakarta.persistence.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = "districts")
@EqualsAndHashCode(exclude = "districts")
@Table(name = "cities")
public class City {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name_uk", nullable = false)
	private String city;

	@Column(name = "name_en", nullable = false)
	private String cityEn;

	@ManyToOne
	private Region region;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "city", fetch = FetchType.LAZY)
	private List<District> districts;
}
