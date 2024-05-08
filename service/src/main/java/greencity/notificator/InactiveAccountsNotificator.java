package greencity.notificator;

import greencity.dto.notification.ScheduledNotificationDto;
import greencity.notificator.scheduler.NotificationTaskScheduler;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.notificator.ScheduledNotificator;
import greencity.service.ubs.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static greencity.enums.NotificationType.LETS_STAY_CONNECTED;

@Component
@RequiredArgsConstructor
public class InactiveAccountsNotificator implements ScheduledNotificator {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationService notificationService;
    private final NotificationTaskScheduler taskScheduler;

    @Override
    public ScheduledNotificationDto notifyBySchedule() {
        var notificationSchedule =
            notificationTemplateRepository.findScheduleOfActiveTemplateByType(LETS_STAY_CONNECTED);
        return createNotificationScheduler(notificationSchedule);
    }

    private ScheduledNotificationDto createNotificationScheduler(String schedule) {
        return taskScheduler.scheduleNotification(notificationService::notifyInactiveAccounts,
            schedule, LETS_STAY_CONNECTED, this.getClass());
    }
}