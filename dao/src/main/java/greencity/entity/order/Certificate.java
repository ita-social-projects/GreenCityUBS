package greencity.entity.order;

import greencity.entity.enums.CertificateStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(exclude = {"order"})
@Table(name = "certificate")
public class Certificate {
    @Id
    @Column(length = 9)
    private String code;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CertificateStatus certificateStatus;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column
    private Integer points;

    @Column
    private LocalDate expirationDate;

    @Column
    private LocalDate creationDate;

    @Column
    private LocalDate dateOfUse;

    @Column(name = "initial_points_value")
    private Integer initialPointsValue;
}
