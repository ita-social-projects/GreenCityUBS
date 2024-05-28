package greencity.notificator;

import greencity.dto.notification.ScheduledNotificationDto;
import greencity.notificator.scheduler.NotificationTaskScheduler;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.notificator.ScheduledNotificator;
import greencity.service.ubs.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static greencity.enums.NotificationType.VIOLATION_THE_RULES;

@Component
@RequiredArgsConstructor
public class AddedViolationNotificator implements ScheduledNotificator {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationTaskScheduler taskScheduler;
    private final NotificationService notificationService;

    @Override
    public ScheduledNotificationDto notifyBySchedule() {
        var notificationSchedule =
            notificationTemplateRepository.findScheduleOfActiveTemplateByType(VIOLATION_THE_RULES);
        return createNotificationScheduler(notificationSchedule);
    }

    private ScheduledNotificationDto createNotificationScheduler(String schedule) {
        return taskScheduler.scheduleNotification(notificationService::notifyAllAddedViolations,
            schedule, VIOLATION_THE_RULES, this.getClass());
    }
}