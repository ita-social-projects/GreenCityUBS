package greencity.notificator;

import greencity.dto.notification.ScheduledNotificationDto;
import greencity.notificator.scheduler.NotificationTaskScheduler;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.ubs.NotificationService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static greencity.dto.notification.ScheduledNotificationDto.build;
import static greencity.enums.NotificationType.CUSTOM;

@Component
@RequiredArgsConstructor
public class CustomNotificationsNotificator implements ScheduledNotificator {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationTaskScheduler taskScheduler;
    private final NotificationService notificationService;
    private final List<ScheduledFuture<?>> scheduledFutures = new ArrayList<>();

    @Override
    public ScheduledNotificationDto notifyBySchedule() {
        closePreviousTasksIfPresent();
        var templates = notificationTemplateRepository.findAllActiveCustomNotificationsTemplates();
        templates.forEach(
            template -> createNotificationScheduler(template.getTemplateUuid(), template.getSchedule()));
        return build(CUSTOM, this.getClass());
    }

    private void closePreviousTasksIfPresent() {
        scheduledFutures.forEach(this::closePreviousTaskIfPresent);
        scheduledFutures.clear();
    }

    private void createNotificationScheduler(String templateUuid, String schedule) {
        scheduledFutures.add(taskScheduler.scheduleNotification(
            () -> notificationService.notifyCustom(templateUuid), schedule, CUSTOM));
    }
}
