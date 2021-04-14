package greencity.entity.order;

import greencity.entity.language.Language;
import javax.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "bag_translations")
public class BagTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String name;

    @ManyToOne
    private Bag bag;

    @ManyToOne
    private Language language;
}
