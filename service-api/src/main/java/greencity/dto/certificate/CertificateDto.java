package greencity.dto.certificate;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateDto {
    private String certificateStatus;
    private Integer points;
    private LocalDate dateOfUse;
    private LocalDate expirationDate;
    private String code;
}
