package greencity.mapping;

import greencity.dto.CertificateDtoForAdding;
import greencity.dto.CertificateDtoForSearching;
import greencity.entity.enums.CertificateStatus;
import greencity.entity.order.Certificate;
import java.time.LocalDate;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class CertificateDtoForAddingMapper extends AbstractConverter<CertificateDtoForAdding, Certificate> {
    @Override
    protected Certificate convert(CertificateDtoForAdding source) {
        Certificate certificate = Certificate.builder()
            .code(source.getCode())
            .points(source.getPoints())
            .creationDate(LocalDate.now())
            .expirationDate(LocalDate.now().plusMonths(source.getMonthCount()))
            .certificateStatus(CertificateStatus.ACTIVE)
            .build();
        return certificate;
    }
}
