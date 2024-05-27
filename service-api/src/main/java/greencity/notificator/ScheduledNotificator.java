package greencity.notificator;

import greencity.dto.notification.ScheduledNotificationDto;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

public interface ScheduledNotificator {
    ScheduledNotificationDto notifyBySchedule();

    default void closePreviousTaskIfPresent(ScheduledFuture<?> scheduledFuture) {
        if (Objects.nonNull(scheduledFuture)) {
            scheduledFuture.cancel(true);
        }
    }
}
