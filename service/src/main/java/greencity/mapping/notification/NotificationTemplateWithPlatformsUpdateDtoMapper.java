package greencity.mapping.notification;

import greencity.dto.notification.NotificationTemplateUpdateInfoDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsUpdateDto;
import greencity.entity.notifications.NotificationTemplate;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class NotificationTemplateWithPlatformsUpdateDtoMapper
    extends AbstractConverter<NotificationTemplate, NotificationTemplateWithPlatformsUpdateDto> {
    @Override
    protected NotificationTemplateWithPlatformsUpdateDto convert(NotificationTemplate notificationTemplate) {
        return NotificationTemplateWithPlatformsUpdateDto.builder()
            .notificationTemplateUpdateInfo(NotificationTemplateUpdateInfoDto.builder()
                .type(notificationTemplate.getNotificationType())
                .trigger(notificationTemplate.getTrigger())
                .time(notificationTemplate.getTime())
                .schedule(notificationTemplate.getSchedule())
                .titleUk(notificationTemplate.getTitleUk())
                .titleEn(notificationTemplate.getTitleEn())
                .userCategory(notificationTemplate.getUserCategory())
                .build())
            .platforms(notificationTemplate.getNotificationPlatforms().stream()
                .map(NotificationMappers::toNotificationPlatformDto)
                .toList())
            .build();
    }
}
