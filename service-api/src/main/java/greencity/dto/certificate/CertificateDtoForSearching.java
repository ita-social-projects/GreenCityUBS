package greencity.dto.certificate;

import greencity.enums.CertificateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
