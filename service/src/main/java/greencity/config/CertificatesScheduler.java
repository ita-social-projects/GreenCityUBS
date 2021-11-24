package greencity.config;

import greencity.service.ubs.CertificatesActualityServiceImpl;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@NoArgsConstructor
@Slf4j
public class CertificatesScheduler {
    @Autowired
    private CertificatesActualityServiceImpl certificatesActualityService;

    /**
     * Method checks all certificates id DB and change status those whose expiration
     * date has expired.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Kiev")
    public void checkCertificatesForActuality() {
        log.info("Changing certificates status if expiration date has expired");
        certificatesActualityService.checkCertificatesForActuality();
    }
}
