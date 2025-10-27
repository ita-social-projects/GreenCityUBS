package greencity.dto.notification;

import greencity.enums.NotificationType;
import greencity.notificator.ScheduledNotificator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
