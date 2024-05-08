package greencity.notificator;

import greencity.dto.notification.ScheduledNotificationDto;
import greencity.notificator.scheduler.NotificationTaskScheduler;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.notificator.ScheduledNotificator;
import greencity.service.ubs.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static greencity.enums.NotificationType.ORDER_STATUS_CHANGED;

@Component
@RequiredArgsConstructor
public class ChangedStatusNotificator implements ScheduledNotificator {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationService notificationService;
    private final NotificationTaskScheduler taskScheduler;

    @Override
    public ScheduledNotificationDto notifyBySchedule() {
        var notificationSchedule = notificationTemplateRepository
            .findScheduleOfActiveTemplateByType(ORDER_STATUS_CHANGED);

        return createNotificationScheduler(notificationSchedule);
    }

    private ScheduledNotificationDto createNotificationScheduler(String schedule) {
        return taskScheduler.scheduleNotification(notificationService::notifyAllChangedOrderStatuses, schedule,
            ORDER_STATUS_CHANGED, this.getClass());
    }
}
