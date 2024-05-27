package greencity.notificator.listener;

import greencity.constant.AppConstant;
import greencity.dto.notification.ScheduledNotificationDto;
import greencity.enums.NotificationType;
import greencity.service.notificator.ScheduledNotificator;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPlanner {
    private final List<ScheduledNotificator> scheduledNotificators;

    private final List<ScheduledNotificationDto> scheduledNotificationDtos = new ArrayList<>();

    @EventListener(value = ApplicationReadyEvent.class)
    public void scheduleNotifications() {
        scheduledNotificators.forEach(this::createNotification);
    }

    private void createNotification(ScheduledNotificator scheduledNotificator) {
        scheduledNotificationDtos.add(scheduledNotificator.notifyBySchedule());
    }

    public void restartNotificator(NotificationType notificationType) {
        scheduledNotificationDtos.stream()
            .filter(dto -> dto.getNotificationType().equals(notificationType))
            .findAny()
            .ifPresent(dto -> findScheduledNotificatorByType(dto)
                .ifPresent(scheduledNotificator -> restart(scheduledNotificator, dto)));
    }

    private Optional<ScheduledNotificator> findScheduledNotificatorByType(ScheduledNotificationDto dto) {
        return scheduledNotificators.stream()
            .filter(scheduledNotificator -> scheduledNotificator.getClass().equals(dto.getType()))
            .findAny();
    }

    private void restart(ScheduledNotificator scheduledNotificator, ScheduledNotificationDto scheduledNotificationDto) {
        restartSchedule(scheduledNotificationDto);
        scheduledNotificationDtos.remove(scheduledNotificationDto);
        scheduledNotificationDtos.add(scheduledNotificator.notifyBySchedule());
        log.info(AppConstant.NOTIFICATOR_RESTART_LOG_MESSAGE, scheduledNotificator.getClass());
    }

    private void restartSchedule(ScheduledNotificationDto scheduledNotification) {
        if (Objects.nonNull(scheduledNotification.getScheduledFuture())) {
            scheduledNotification.getScheduledFuture().cancel(true);
        }
    }
}