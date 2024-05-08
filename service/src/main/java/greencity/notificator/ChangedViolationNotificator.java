package greencity.notificator;

import greencity.dto.notification.ScheduledNotificationDto;
import greencity.notificator.scheduler.NotificationTaskScheduler;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.notificator.ScheduledNotificator;
import greencity.service.ubs.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static greencity.enums.NotificationType.CHANGED_IN_RULE_VIOLATION_STATUS;

@Component
@RequiredArgsConstructor
public class ChangedViolationNotificator implements ScheduledNotificator {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationTaskScheduler taskScheduler;
    private final NotificationService notificationService;

    @Override
    public ScheduledNotificationDto notifyBySchedule() {
        var notificationSchedule =
            notificationTemplateRepository.findScheduleOfActiveTemplateByType(CHANGED_IN_RULE_VIOLATION_STATUS);
        return createNotificationScheduler(notificationSchedule);
    }

    private ScheduledNotificationDto createNotificationScheduler(String schedule) {
        return taskScheduler.scheduleNotification(notificationService::notifyAllChangedViolations,
            schedule, CHANGED_IN_RULE_VIOLATION_STATUS, this.getClass());
    }
}