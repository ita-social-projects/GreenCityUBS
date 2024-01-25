package greencity.config;

import greencity.service.notification.NotificationServiceImpl;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.Clock;
import java.time.ZoneId;

@Slf4j
@Configuration
@EnableScheduling
@NoArgsConstructor
public class NotificationScheduler {
    @Bean
    Clock kyivZonedClock() {
        return Clock.system(ZoneId.of("Europe/Kiev"));
    }

    @Autowired
    private NotificationServiceImpl notificationService;

    /**
     * System checks BD at 18.00 daily and sends message in case the order was
     * formed 3 days ago and wasn’t paid by client. Repeat every week over the month
     */
    @Scheduled(cron = "0 0 18 * * ?", zone = "Europe/Kiev")
    public void notifyUnpaidOrders() {
        log.info("Notifying unpaid orders");
        notificationService.notifyUnpaidOrders();
    }

    /**
     * System checks BD at 18.00 daily and sends message in case the order was half
     * paid by client. Repeat every week over the month
     */
    @Scheduled(cron = "0 0 18 * * ?", zone = "Europe/Kiev")
    public void notifyHalfPaidPackages() {
        log.info("Notifying half paid orders");
        notificationService.notifyAllHalfPaidPackages();
    }

    /**
     * System checks BD at 18.00 daily and sends message in case if user's last
     * order was more than 2 month. Repeat every 2 month over the year
     */
    @Scheduled(cron = "0 0 18 * * ?", zone = "Europe/Kiev")
    public void notifyInactiveAccount() {
        log.info("Notifying inactive users");
        notificationService.notifyInactiveAccounts();
    }
}
