package greencity.dto.notification;

import greencity.enums.NotificationType;
import greencity.service.notificator.ScheduledNotificator;
import java.util.concurrent.ScheduledFuture;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ScheduledNotificationDto {
    private NotificationType notificationType;
    private ScheduledFuture<?> scheduledFuture;
    private Class<? extends ScheduledNotificator> type;

    public ScheduledNotificationDto(NotificationType notificationType, ScheduledFuture<?> scheduledFuture) {
        this.notificationType = notificationType;
        this.scheduledFuture = scheduledFuture;
    }
}
