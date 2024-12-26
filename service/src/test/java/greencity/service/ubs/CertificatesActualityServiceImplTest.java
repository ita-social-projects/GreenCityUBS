package greencity.service.ubs;

import greencity.repository.CertificateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CertificatesActualityServiceImplTest {

    @InjectMocks
    CertificatesActualityServiceImpl certificatesActualityService;

    @Mock
    CertificateRepository certificateRepository;

    @Test
    void checkCertificatesForActuality() {
        certificatesActualityService.checkCertificatesForActuality();
        verify(certificateRepository).updateCertificateStatusToExpired();
    }
}
