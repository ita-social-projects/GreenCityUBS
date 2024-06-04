package greencity.mapping.notification;

import greencity.dto.notification.NotificationPlatformDto;
import greencity.dto.notification.NotificationTemplateUpdateInfoDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsUpdateDto;
import greencity.entity.notifications.NotificationTemplate;
import java.util.stream.Collectors;
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
                .title(notificationTemplate.getTitle())
                .titleEng(notificationTemplate.getTitleEng())
                .userCategory(notificationTemplate.getUserCategory())
                .build())
            .platforms(notificationTemplate.getNotificationPlatforms().stream()
                .map(platform -> NotificationPlatformDto.builder()
                    .id(platform.getId())
                    .receiverType(platform.getNotificationReceiverType())
                    .nameEng(platform
                        .getNotificationReceiverType()
                        .getName())
                    .body(platform.getBody())
                    .bodyEng(platform.getBodyEng())
                    .status(platform.getNotificationStatus())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
