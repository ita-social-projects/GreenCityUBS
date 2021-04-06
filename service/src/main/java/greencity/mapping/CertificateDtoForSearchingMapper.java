package greencity.mapping;

import greencity.dto.CertificateDtoForSearching;
import greencity.entity.order.Certificate;
import org.modelmapper.AbstractConverter;
import org.modelmapper.spi.MappingContext;
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
            .build();
        if (source.getOrder() == null) {
            build.setOrderId(null);
        } else {
            build.setOrderId(source.getOrder().getId());
        }
        return build;
    }
}
