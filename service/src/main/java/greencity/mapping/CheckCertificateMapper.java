package greencity.mapping;

import greencity.dto.certificate.CertificateDto;
import greencity.entity.order.Certificate;
import org.modelmapper.AbstractConverter;

public class CheckCertificateMapper extends AbstractConverter<Certificate, CertificateDto> {
    @Override
    protected CertificateDto convert(Certificate certificate) {
        return CertificateDto.builder()
            .certificateStatus(certificate.getCertificateStatus().name())
            .creationDate(certificate.getCreationDate())
            .expirationDate(certificate.getExpirationDate())
            .dateOfUse(certificate.getDateOfUse())
            .points(certificate.getPoints())
            .code(certificate.getCode())
            .build();
    }
}
