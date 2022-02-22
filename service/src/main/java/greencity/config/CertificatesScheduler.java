package greencity.config;

import greencity.service.ubs.CertificatesActualityServiceImpl;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.swing.text.DateFormatter;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;

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
    @Scheduled(cron = "${greencity.schedule-constants.certificates-scheduler.cron}", zone = "${greencity.schedule-constants.zone}")
    public void checkCertificatesForActuality() {
        log.info("Changing certificates status if expiration date has expired");
        certificatesActualityService.checkCertificatesForActuality();
    }
}
