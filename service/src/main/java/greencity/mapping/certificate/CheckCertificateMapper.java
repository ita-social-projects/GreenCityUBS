package greencity.mapping.certificate;

import greencity.dto.certificate.CertificateDto;
import greencity.entity.order.Certificate;
import org.modelmapper.AbstractConverter;

public class CheckCertificateMapper extends AbstractConverter<Certificate, CertificateDto> {
    @Override
    protected CertificateDto convert(Certificate certificate) {
        return CertificateDto.builder()
            .certificateStatus(certificate.getCertificateStatus().name())
            .creationDate(certificate.getCreationDate())
            .expirationDate(certificate.getExpirationDate() != null ? certificate.getExpirationDate() : null)
            .dateOfUse(certificate.getDateOfUse() != null ? certificate.getDateOfUse() : null)
            .points(certificate.getPoints())
            .code(certificate.getCode())
            .build();
    }
}
