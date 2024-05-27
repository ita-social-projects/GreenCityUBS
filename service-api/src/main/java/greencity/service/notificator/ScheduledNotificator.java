package greencity.service.notificator;

import greencity.dto.notification.ScheduledNotificationDto;

public interface ScheduledNotificator {
    ScheduledNotificationDto notifyBySchedule();
}
