package greencity.service.ubs;

import greencity.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class CertificatesActualityServiceImpl implements CertificatesActualityService {
    private final CertificateRepository certificateRepository;

    /**
     * Method update certificates status to expired instead of Active or New in case
     * the expiration date is off.
     *
     * @author Sikhovskiy Rostyslav
     */
    @Override
    @Transactional
    public void checkCertificatesForActuality() {
        certificateRepository.updateCertificateStatusToExpired();
    }
}
