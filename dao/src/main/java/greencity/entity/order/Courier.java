package greencity.entity.order;

import greencity.entity.enums.CourierStatus;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(
    exclude = {"courierTranslationList", "courierLocations", "services"})
@Table(name = "courier")
public class Courier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CourierStatus courierStatus;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courier", fetch = FetchType.LAZY)
    private List<CourierLocations> courierLocations;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courier", fetch = FetchType.LAZY)
    private List<CourierTranslation> courierTranslationList;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "courier")
    List<Service> services;
}
