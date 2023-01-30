package greencity.config;

import greencity.service.ubs.UBSManagementServiceImpl;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@NoArgsConstructor
public class OrderStatusScheduler {
    @Autowired
    UBSManagementServiceImpl ubsManagementService;

    /**
     * System checks BD at 00.00 daily and sends a message if the status of the
     * order has changed to "ON_THE_ROUTE" on the day of export.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Kiev")
    public void autoChangeOrderStatusOnTheDayOfExport() {
        ubsManagementService.updateOrderStatusOnTheDayOfExport();
    }
}
