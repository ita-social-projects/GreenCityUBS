package greencity.filters;

import greencity.entity.enums.CertificateStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateFilterCriteria {
    private CertificateStatus[] certificateStatus;
    private String expirationDateFrom;
    private String expirationDateTo;
    private String creationDateFrom;
    private String creationDateTo;
    private String dateOfUseFrom;
    private String dateOfUseTo;
    private Integer[] points;
    private String search;
}
