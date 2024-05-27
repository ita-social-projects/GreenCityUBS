package greencity.notificator;

import greencity.dto.notification.ScheduledNotificationDto;
import greencity.notificator.scheduler.NotificationTaskScheduler;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.notificator.ScheduledNotificator;
import greencity.service.ubs.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static greencity.enums.NotificationType.HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF;

@Component
@RequiredArgsConstructor
public class HalfPaidBroughtByHimselfNotificator implements ScheduledNotificator {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationService notificationService;
    private final NotificationTaskScheduler taskScheduler;

    @Override
    public ScheduledNotificationDto notifyBySchedule() {
        var notificationSchedule = notificationTemplateRepository
            .findScheduleOfActiveTemplateByType(HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF);

        return createNotificationScheduler(notificationSchedule);
    }

    private ScheduledNotificationDto createNotificationScheduler(String schedule) {
        return taskScheduler.scheduleNotification(
            notificationService::notifyAllHalfPaidOrdersWithStatusBroughtByHimself, schedule,
            HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF, this.getClass());
    }
}
