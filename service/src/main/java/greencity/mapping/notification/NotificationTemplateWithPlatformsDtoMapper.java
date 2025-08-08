package greencity.mapping.notification;

import greencity.dto.notification.NotificationTemplateWithPlatformsDto;
import greencity.entity.notifications.NotificationTemplate;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
class NotificationTemplateWithPlatformsDtoMapper
    extends AbstractConverter<NotificationTemplate, NotificationTemplateWithPlatformsDto> {
    @Override
    protected NotificationTemplateWithPlatformsDto convert(NotificationTemplate notificationTemplate) {
        return NotificationTemplateWithPlatformsDto.builder()
            .notificationTemplateMainInfoDto(
                NotificationMappers.toNotificationTemplateMainInfoDto.apply(notificationTemplate))
            .platforms(notificationTemplate.getNotificationPlatforms().stream()
                .map(NotificationMappers.toNotificationPlatformDto)
                .toList())
            .build();
    }
}
