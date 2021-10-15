package greencity.entity.order;

import greencity.entity.enums.MinAmountOfBag;
import greencity.entity.user.Location;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
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

    @Column
    private Integer commission;

    @Column(nullable = false)
    private Integer fullPrice;

    @Column(nullable = false)
    private LocalDate createdAt;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private LocalDate editedAt;

    @Column(nullable = false)
    private String editedBy;

    @Column(nullable = false, name = "min_amount_of_bags")
    @Enumerated(EnumType.STRING)
    private MinAmountOfBag minAmountOfBags;

    @ManyToOne
    private Location location;

    @OneToMany(mappedBy = "bag", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BagTranslation> bagTranslations;
}
