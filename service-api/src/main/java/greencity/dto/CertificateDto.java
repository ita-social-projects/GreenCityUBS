package greencity.dto;

import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateDto {
    private String certificateStatus;
    private Integer certificatePoints;
    private LocalDate certificateDate;
}
