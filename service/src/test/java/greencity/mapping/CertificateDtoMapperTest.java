package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.CertificateDto;
import greencity.entity.order.Certificate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CertificateDtoMapperTest {
    @InjectMocks
    CertificateDtoMapper certificateDtoMapper;

    @Test
    void convert() {
        Certificate certificate = ModelUtils.getCertificate();
        CertificateDto certificateDto = certificateDtoMapper.convert(certificate);

        Assertions.assertEquals(certificate.getCode(), certificateDto.getCode());
        Assertions.assertEquals(certificate.getPoints(), certificateDto.getPoints());
        Assertions.assertEquals(certificate.getCreationDate(), certificateDto.getCreationDate());
        Assertions.assertEquals(certificate.getCertificateStatus().name(), certificateDto.getCertificateStatus());
    }
}
