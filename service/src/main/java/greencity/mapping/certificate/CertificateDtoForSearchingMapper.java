package greencity.mapping.certificate;

import greencity.dto.certificate.CertificateDtoForSearching;
import greencity.entity.order.Certificate;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class CertificateDtoForSearchingMapper extends AbstractConverter<Certificate, CertificateDtoForSearching> {
    @Override
    protected CertificateDtoForSearching convert(Certificate source) {
        CertificateDtoForSearching build = CertificateDtoForSearching.builder()
            .certificateStatus(source.getCertificateStatus())
            .code(source.getCode())
            .creationDate(source.getCreationDate())
            .expirationDate(source.getExpirationDate())
            .points(source.getPoints())
            .initialPointsValue(source.getInitialPointsValue())
            .dateOfUse(source.getDateOfUse())
            .build();
        if (source.getOrder() == null) {
            build.setOrderId(null);
        } else {
            build.setOrderId(source.getOrder().getId());
        }
        return build;
    }
}
