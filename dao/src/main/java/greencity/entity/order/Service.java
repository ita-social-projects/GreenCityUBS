package greencity.entity.order;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(
    exclude = {"serviceTranslations", "courier", "tariffsInfo"})
@ToString(
    exclude = {"serviceTranslations", "courier", "tariffsInfo"})
@Table(name = "service")

public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Integer capacity;

    @Column(nullable = false)
    private Integer basePrice;

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

    @ManyToOne
    Courier courier;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<ServiceTranslation> serviceTranslations;

    @ManyToOne
    private TariffsInfo tariffsInfo;
}
