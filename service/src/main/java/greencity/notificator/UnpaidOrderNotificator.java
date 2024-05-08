package greencity.notificator;

import greencity.dto.notification.ScheduledNotificationDto;
import greencity.notificator.scheduler.NotificationTaskScheduler;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.notificator.ScheduledNotificator;
import greencity.service.ubs.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static greencity.enums.NotificationType.UNPAID_ORDER;

@Component
@RequiredArgsConstructor
public class UnpaidOrderNotificator implements ScheduledNotificator {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationService notificationService;
    private final NotificationTaskScheduler taskScheduler;

    @Override
    public ScheduledNotificationDto notifyBySchedule() {
        var schedule = notificationTemplateRepository.findScheduleOfActiveTemplateByType(UNPAID_ORDER);
        return taskScheduler.scheduleNotification(notificationService::notifyUnpaidOrders,
            schedule, UNPAID_ORDER, this.getClass());
    }
}