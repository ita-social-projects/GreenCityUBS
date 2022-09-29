package greencity.entity.order;

import lombok.*;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"courier"})
@ToString(exclude = {"courier"})
@Table(name = "courier_translations")
public class CourierTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nameEng;

    @ManyToOne
    private Courier courier;
}