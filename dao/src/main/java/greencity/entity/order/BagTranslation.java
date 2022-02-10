package greencity.entity.order;

import greencity.entity.language.Language;
import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"bag", "language"})
@ToString(exclude = {"bag", "language"})
@Getter
@Setter
@Builder
@Table(name = "bag_translations")
public class BagTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, length = 60)
    private String name;

    @ManyToOne
    private Bag bag;

    @Column(nullable = false, length = 60)
    private String nameEng;
}
