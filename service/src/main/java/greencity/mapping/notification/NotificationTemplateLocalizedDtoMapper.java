package greencity.mapping.notification;

import greencity.dto.notification.NotificationTemplateLocalizedDto;
import greencity.entity.notifications.NotificationTemplate;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class NotificationTemplateLocalizedDtoMapper
    extends AbstractConverter<NotificationTemplate, NotificationTemplateLocalizedDto> {
    @Override
    protected NotificationTemplateLocalizedDto convert(NotificationTemplate notificationTemplate) {
        return NotificationTemplateLocalizedDto.builder()
            .id(notificationTemplate.getId())
            .notificationType(notificationTemplate.getNotificationType().toString())
            .notificationReceiverType(notificationTemplate.getNotificationReceiverType().toString())
            .build();
    }
}