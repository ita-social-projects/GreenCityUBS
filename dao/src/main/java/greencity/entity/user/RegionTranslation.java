package greencity.entity.user;

import greencity.entity.language.Language;
import lombok.*;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "region_translations")
@EqualsAndHashCode(exclude = {"region", "language"})
@ToString(exclude = {"region", "language"})
@Entity
public class RegionTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @ManyToOne
    private Region region;

    @ManyToOne
    private Language language;
}
