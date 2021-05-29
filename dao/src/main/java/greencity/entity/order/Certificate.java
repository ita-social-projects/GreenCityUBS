package greencity.entity.order;

import greencity.entity.enums.CertificateStatus;
import java.time.LocalDate;
import java.util.Date;
import javax.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(exclude = {"certificateStatus", "order", "points", "creationDate", "expirationDate"})
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

    private Integer points;

    @Column
    private LocalDate expirationDate;

    @Column
    private LocalDate creationDate;
}
