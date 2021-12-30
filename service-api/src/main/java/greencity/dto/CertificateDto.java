package greencity.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateDto {
    private String certificateStatus;
    private Integer certificatePoints;
    private LocalDate certificateDate;
    private String code;
}
