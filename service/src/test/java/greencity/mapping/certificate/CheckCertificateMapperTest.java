package greencity.mapping.certificate;

import greencity.ModelUtils;
import greencity.dto.certificate.CertificateDto;
import greencity.entity.order.Certificate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckCertificateMapperTest {
    @InjectMocks
    CheckCertificateMapper checkCertificateMapper;

    @Test
    void convert() {
        Certificate certificate = ModelUtils.getCertificate();
        CertificateDto certificateDto = checkCertificateMapper.convert(certificate);

        Assertions.assertEquals(certificate.getCode(), certificateDto.getCode());
        Assertions.assertEquals(certificate.getPoints(), certificateDto.getPoints());
        Assertions.assertEquals(certificate.getCreationDate(), certificateDto.getCreationDate());
        Assertions.assertEquals(certificate.getDateOfUse(), certificateDto.getDateOfUse());
        Assertions.assertEquals(certificate.getExpirationDate(), certificateDto.getExpirationDate());
        Assertions.assertEquals(certificate.getCertificateStatus().name(), certificateDto.getCertificateStatus());
    }
}
