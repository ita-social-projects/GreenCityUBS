package greencity.entity.order;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(
    exclude = {"bagTranslations"})
@ToString(
    exclude = {"bagTranslations"})
@Builder
@Table(name = "bag")
public class Bag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer price;

    @OneToMany(mappedBy = "bag", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BagTranslation> bagTranslations;
}
