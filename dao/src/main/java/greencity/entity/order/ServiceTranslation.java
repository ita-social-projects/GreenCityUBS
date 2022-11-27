package greencity.entity.order;

import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"service"})
@ToString(exclude = {"service"})
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
    private String nameEng;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String descriptionEng;
    @ManyToOne
    Service service;
}
