package greencity.entity.order;

import greencity.entity.enums.CourierStatus;
import greencity.entity.user.User;
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
    exclude = {"courierTranslationList", "services", "tariffsInfoList"})
@Table(name = "courier")
public class Courier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CourierStatus courierStatus;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courier", fetch = FetchType.LAZY)
    private List<CourierTranslation> courierTranslationList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courier", fetch = FetchType.LAZY)
    private List<TariffsInfo> tariffsInfoList;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "courier")
    List<Service> services;

    @ManyToOne
    private User createdBy;

    private LocalDate createDate;
}
