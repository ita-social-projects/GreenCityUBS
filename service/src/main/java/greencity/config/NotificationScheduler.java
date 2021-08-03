package greencity.config;

import greencity.service.NotificationServiceImpl;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@NoArgsConstructor
public class NotificationScheduler {
    @Autowired
    private NotificationServiceImpl notificationService;

    /**
     * System checks BD at 18.00 daily and sends messages in case the order was formed 3 days ago
     * and wasn’t paid by client
     */
    @Scheduled(cron = "0 0/57 22 * * ?", zone = "Europe/Kiev")
    public void notifyUnpaidOrders() {
        notificationService.notifyUnpaidOrders();
    }

    @Scheduled(cron = "0 0 18 * * ?", zone = "Europe/Kiev")
    public void notifyHalfPaidPackages() {
        notificationService.notifyAllHalfPaidPackages();
    }

    @Scheduled(cron = "0 0 18 * * ?", zone = "Europe/Kiev")
    public void notifyInactiveAccount() {
        notificationService.notifyInactiveAccounts();
    }
}
