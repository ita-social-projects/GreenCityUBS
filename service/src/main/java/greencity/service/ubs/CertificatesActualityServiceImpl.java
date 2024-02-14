package greencity.service.ubs;

import greencity.repository.CertificateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
public class CertificatesActualityServiceImpl implements CertificatesActualityService {
    @Autowired
    CertificateRepository certificateRepository;

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
