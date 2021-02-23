package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateDto {
    private String certificateStatus;
    private Integer certificatePoints;
}
