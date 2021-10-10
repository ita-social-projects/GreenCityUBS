package greencity.entity.order;

import greencity.entity.language.Language;
import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"service", "language"})
@ToString(exclude = {"service", "language"})
@Getter
@Setter
@Builder
@Table(name = "service_translations")
public class ServiceTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    Service service;

    @ManyToOne
    private Language language;
}
