package greencity.notificator;

import greencity.dto.notification.ScheduledNotificationDto;
import greencity.notificator.scheduler.NotificationTaskScheduler;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.ubs.NotificationService;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static greencity.dto.notification.ScheduledNotificationDto.build;
import static greencity.enums.NotificationType.VIOLATION_THE_RULES;

@Component
@RequiredArgsConstructor
public class AddedViolationNotificator implements ScheduledNotificator {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationTaskScheduler taskScheduler;
    private final NotificationService notificationService;
    private ScheduledFuture<?> scheduledFuture;

    @Override
    public ScheduledNotificationDto notifyBySchedule() {
        closePreviousTaskIfPresent(scheduledFuture);
        var notificationSchedule =
            notificationTemplateRepository.findScheduleOfActiveTemplateByType(VIOLATION_THE_RULES);
        return createNotificationScheduler(notificationSchedule);
    }

    private ScheduledNotificationDto createNotificationScheduler(String schedule) {
        scheduledFuture = taskScheduler.scheduleNotification(notificationService::notifyAllAddedViolations,
            schedule, VIOLATION_THE_RULES);
        return build(VIOLATION_THE_RULES, this.getClass());
    }
}