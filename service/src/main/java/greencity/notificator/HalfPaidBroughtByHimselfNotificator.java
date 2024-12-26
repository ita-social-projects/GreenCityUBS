package greencity.notificator;

import greencity.dto.notification.ScheduledNotificationDto;
import greencity.notificator.scheduler.NotificationTaskScheduler;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.ubs.NotificationService;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static greencity.dto.notification.ScheduledNotificationDto.build;
import static greencity.enums.NotificationType.HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF;

@Component
@RequiredArgsConstructor
public class HalfPaidBroughtByHimselfNotificator implements ScheduledNotificator {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationService notificationService;
    private final NotificationTaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledFuture;

    @Override
    public ScheduledNotificationDto notifyBySchedule() {
        closePreviousTaskIfPresent(scheduledFuture);
        var notificationSchedule = notificationTemplateRepository
            .findScheduleOfActiveTemplateByType(HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF);

        return createNotificationScheduler(notificationSchedule);
    }

    private ScheduledNotificationDto createNotificationScheduler(String schedule) {
        scheduledFuture = taskScheduler.scheduleNotification(
            notificationService::notifyAllHalfPaidOrdersWithStatusBroughtByHimself, schedule,
            HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF);
        return build(HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF, this.getClass());
    }
}
