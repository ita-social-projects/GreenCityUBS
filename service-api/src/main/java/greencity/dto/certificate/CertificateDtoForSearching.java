package greencity.dto.certificate;

import greencity.enums.CertificateStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CertificateDtoForSearching {
    private String code;
    private CertificateStatus certificateStatus;
    private Long orderId;
    private Integer points;
    private LocalDate expirationDate;
    private LocalDate creationDate;
    private LocalDate dateOfUse;
    private Integer initialPointsValue;
}
