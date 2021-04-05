package greencity.config;

import greencity.entity.order.Certificate;
import greencity.repository.CertificateRepository;
import greencity.ubstelegrambot.UBSBotService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableScheduling
@AllArgsConstructor
public class SenderScheduledMessages {
    private final UBSBotService ubsBotService;
    private final CertificateRepository certificateRepository;

    /**
     * The method every day at 09:00 am send a message to users that have not paid
     * of orders within three days.
     */
    @Scheduled(cron = "0 0 9 * * ?", zone = "Europe/Kiev")
    public void sendMessageWhenOrderNonPayment() {
        ubsBotService.sendMessageWhenOrderNonPayment();
    }

    /**
     * The method every day at 08:00 am send a message to users date and time when
     * garbage truck arrives.
     */
    @Scheduled(cron = "0 0 8 * * ?", zone = "Europe/Kiev")
    public void sendMessageWhenGarbageTruckArrives() {
        ubsBotService.sendMessageWhenGarbageTruckArrives();
    }

    /**
     * Method schedules updating a {@link Certificate} status to expired for each
     * {@link Certificate} in which expiration date is off every midnight.
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleUpdateExpiredCertificates() {
        certificateRepository.updateCertificateStatusToExpired();
    }
}
