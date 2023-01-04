package greencity.entity.order;

import greencity.enums.CourierStatus;
import greencity.entity.user.User;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(
    exclude = {"services", "tariffsInfoList"})
@Table(name = "courier")
public class Courier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CourierStatus courierStatus;

    @Column(nullable = false)
    private String nameUk;

    @Column(nullable = false)
    private String nameEn;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courier")
    private List<TariffsInfo> tariffsInfoList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courier")
    List<Service> services;

    @ManyToOne
    private User createdBy;

    private LocalDate createDate;
}
