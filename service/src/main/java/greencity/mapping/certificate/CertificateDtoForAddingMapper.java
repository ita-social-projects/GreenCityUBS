package greencity.mapping.certificate;

import greencity.dto.certificate.CertificateDtoForAdding;
import greencity.enums.CertificateStatus;
import greencity.entity.order.Certificate;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class CertificateDtoForAddingMapper extends AbstractConverter<CertificateDtoForAdding, Certificate> {
    @Override
    protected Certificate convert(CertificateDtoForAdding source) {
        return Certificate.builder()
            .code(source.getCode())
            .points(source.getPoints())
            .creationDate(LocalDate.now())
            .expirationDate(LocalDate.now().plusMonths(source.getMonthCount()))
            .certificateStatus(CertificateStatus.ACTIVE)
            .initialPointsValue(source.getInitialPointsValue())
            .build();
    }
}
