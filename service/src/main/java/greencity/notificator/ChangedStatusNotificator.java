package greencity.notificator;

import greencity.dto.notification.ScheduledNotificationDto;
import greencity.notificator.scheduler.NotificationTaskScheduler;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.notificator.ScheduledNotificator;
import greencity.service.ubs.NotificationService;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static greencity.dto.notification.ScheduledNotificationDto.*;
import static greencity.enums.NotificationType.ORDER_STATUS_CHANGED;

@Component
@RequiredArgsConstructor
public class ChangedStatusNotificator implements ScheduledNotificator {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationService notificationService;
    private final NotificationTaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledFuture;

    @Override
    public ScheduledNotificationDto notifyBySchedule() {
        closePreviousTaskIfPresent(scheduledFuture);
        var notificationSchedule = notificationTemplateRepository
            .findScheduleOfActiveTemplateByType(ORDER_STATUS_CHANGED);

        return createNotificationScheduler(notificationSchedule);
    }

    private ScheduledNotificationDto createNotificationScheduler(String schedule) {
        scheduledFuture =
            taskScheduler.scheduleNotification(notificationService::notifyAllChangedOrderStatuses, schedule,
                ORDER_STATUS_CHANGED);
        return build(ORDER_STATUS_CHANGED, this.getClass());
    }
}
