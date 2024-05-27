package greencity.notificator;

import greencity.dto.notification.ScheduledNotificationDto;
import greencity.notificator.scheduler.NotificationTaskScheduler;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.ubs.NotificationService;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static greencity.dto.notification.ScheduledNotificationDto.build;
import static greencity.enums.NotificationType.LETS_STAY_CONNECTED;

@Component
@RequiredArgsConstructor
public class InactiveAccountsNotificator implements ScheduledNotificator {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationService notificationService;
    private final NotificationTaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledFuture;

    @Override
    public ScheduledNotificationDto notifyBySchedule() {
        closePreviousTaskIfPresent(scheduledFuture);
        var notificationSchedule =
            notificationTemplateRepository.findScheduleOfActiveTemplateByType(LETS_STAY_CONNECTED);
        return createNotificationScheduler(notificationSchedule);
    }

    private ScheduledNotificationDto createNotificationScheduler(String schedule) {
        scheduledFuture = taskScheduler.scheduleNotification(notificationService::notifyInactiveAccounts,
            schedule, LETS_STAY_CONNECTED);
        return build(LETS_STAY_CONNECTED, this.getClass());
    }
}