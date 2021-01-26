package greencity.dao.entity.order;

import greencity.dao.entity.enums.CertificateStatus;
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

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CertificateStatus certificate_status;

    @OneToOne(mappedBy = "certificate")
    private Order order;
}
