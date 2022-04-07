package greencity.entity.language;

import greencity.entity.order.CourierTranslation;
import greencity.entity.order.ServiceTranslation;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"bagTranslations", "serviceTranslations", "locationTranslations", "courierTranslations",
    "regionTranslations"})
@ToString(exclude = {"bagTranslations"})
@Builder
@Table(name = "languages")
public class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 35)
    private String code;

    @OneToMany(mappedBy = "language", fetch = FetchType.LAZY)
    private List<ServiceTranslation> serviceTranslations;

    /*-
    @OneToMany(mappedBy = "language", fetch = FetchType.LAZY)
    private List<LocationTranslation> locationTranslations;
    */
    @OneToMany(mappedBy = "language", fetch = FetchType.LAZY)
    private List<CourierTranslation> courierTranslations;
    /*-
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "language")
    private List<RegionTranslation> regionTranslations;
    */
}
