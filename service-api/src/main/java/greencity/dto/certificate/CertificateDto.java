package greencity.dto.certificate;

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
public class CertificateDto {
    private String certificateStatus;
    private Integer points;
    private LocalDate creationDate;
    private LocalDate dateOfUse;
    private LocalDate expirationDate;
    private String code;
}
