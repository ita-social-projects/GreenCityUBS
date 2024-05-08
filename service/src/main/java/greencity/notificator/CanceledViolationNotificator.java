package greencity.notificator;

import greencity.dto.notification.ScheduledNotificationDto;
import greencity.notificator.scheduler.NotificationTaskScheduler;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.notificator.ScheduledNotificator;
import greencity.service.ubs.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static greencity.enums.NotificationType.CANCELED_VIOLATION_THE_RULES_BY_THE_MANAGER;

@Component
@RequiredArgsConstructor
public class CanceledViolationNotificator implements ScheduledNotificator {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationTaskScheduler taskScheduler;
    private final NotificationService notificationService;

    @Override
    public ScheduledNotificationDto notifyBySchedule() {
        var notificationSchedule =
            notificationTemplateRepository
                .findScheduleOfActiveTemplateByType(CANCELED_VIOLATION_THE_RULES_BY_THE_MANAGER);
        return createNotificationScheduler(notificationSchedule);
    }

    private ScheduledNotificationDto createNotificationScheduler(String schedule) {
        return taskScheduler.scheduleNotification(notificationService::notifyAllCanceledViolations,
            schedule, CANCELED_VIOLATION_THE_RULES_BY_THE_MANAGER, this.getClass());
    }
}