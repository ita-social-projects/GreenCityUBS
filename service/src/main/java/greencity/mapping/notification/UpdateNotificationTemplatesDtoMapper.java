package greencity.mapping.notification;

import greencity.dto.notification.UpdateNotificationTemplatesDto;
import greencity.entity.notifications.NotificationTemplate;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class UpdateNotificationTemplatesDtoMapper
    extends AbstractConverter<NotificationTemplate, UpdateNotificationTemplatesDto> {
    @Override
    protected UpdateNotificationTemplatesDto convert(NotificationTemplate notificationTemplate) {
        return UpdateNotificationTemplatesDto.builder()
            .body(notificationTemplate.getBody())
            .notificationType(notificationTemplate.getNotificationType().toString())
            .build();
    }
}
