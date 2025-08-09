package greencity.mapping.notification;

import greencity.dto.notification.NotificationTemplateDto;
import greencity.entity.notifications.NotificationTemplate;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class NotificationTemplateDtoMapper
    extends AbstractConverter<NotificationTemplate, NotificationTemplateDto> {
    @Override
    protected NotificationTemplateDto convert(NotificationTemplate notificationTemplate) {
        return NotificationTemplateDto.builder()
            .id(notificationTemplate.getId())
            .notificationTemplateMainInfoDto(
                NotificationMappers.toNotificationTemplateMainInfoDto(notificationTemplate))
            .build();
    }
}
