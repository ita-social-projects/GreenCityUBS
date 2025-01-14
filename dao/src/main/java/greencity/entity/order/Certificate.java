package greencity.entity.order;

import greencity.enums.CertificateStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
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
