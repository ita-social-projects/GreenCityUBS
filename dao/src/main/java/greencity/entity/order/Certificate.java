package greencity.entity.order;

import greencity.entity.enums.CertificateStatus;
import javax.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "certificate")
public class Certificate {
    @Id
    private String code;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CertificateStatus certificateStatus;

    @OneToOne(mappedBy = "certificate")
    private Order order;

    private Integer points;
}
