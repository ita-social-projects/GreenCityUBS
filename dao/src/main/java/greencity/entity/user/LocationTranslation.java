package greencity.entity.user;

import greencity.entity.language.Language;
import lombok.*;

import javax.persistence.*;

@Entity
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "location_translations")
public class LocationTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_name")
    private String locationName;

    @ManyToOne
    private Location location;

    @ManyToOne
    private Language language;
}
