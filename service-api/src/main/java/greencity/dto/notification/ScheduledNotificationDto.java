package greencity.dto.notification;

import greencity.enums.NotificationType;
import greencity.service.notificator.ScheduledNotificator;
import java.util.concurrent.ScheduledFuture;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ScheduledNotificationDto {
    private NotificationType notificationType;
    private Class<? extends ScheduledNotificator> type;

    public static ScheduledNotificationDto build(
        NotificationType notificationType, Class<? extends ScheduledNotificator> type) {
        return new ScheduledNotificationDto(notificationType, type);
    }
}
