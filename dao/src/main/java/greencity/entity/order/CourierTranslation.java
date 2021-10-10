package greencity.entity.order;

import greencity.entity.language.Language;
import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
@Table(name = "courier_translations")
public class CourierTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    private Courier courier;

    @ManyToOne
    private Language language;
}
