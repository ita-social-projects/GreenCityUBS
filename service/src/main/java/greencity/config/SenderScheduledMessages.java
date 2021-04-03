package greencity.config;

import greencity.ubstelegrambot.UBSBotService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@AllArgsConstructor
public class SenderScheduledMessages {
    private final UBSBotService ubsBotService;

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
}
