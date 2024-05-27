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
import static greencity.enums.NotificationType.DONE_OR_CANCELED_UNPAID_ORDER;

@Component
@RequiredArgsConstructor
public class DoneOrCanceledUnpaidOrderNotificator implements ScheduledNotificator {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationService notificationService;
    private final NotificationTaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledFuture;

    @Override
    public ScheduledNotificationDto notifyBySchedule() {
        closePreviousTaskIfPresent(scheduledFuture);
        var notificationSchedule = notificationTemplateRepository
            .findScheduleOfActiveTemplateByType(DONE_OR_CANCELED_UNPAID_ORDER);

        return createNotificationScheduler(notificationSchedule);
    }

    private ScheduledNotificationDto createNotificationScheduler(String schedule) {
        scheduledFuture =
            taskScheduler.scheduleNotification(notificationService::notifyAllDoneOrCanceledUnpaidOrders, schedule,
                DONE_OR_CANCELED_UNPAID_ORDER);
        return build(DONE_OR_CANCELED_UNPAID_ORDER, this.getClass());
    }
}
