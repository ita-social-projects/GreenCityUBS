package greencity.mapping.notification;

import greencity.dto.notification.NotificationPlatformDto;
import greencity.dto.notification.NotificationTemplateMainInfoDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsDto;
import greencity.entity.notifications.NotificationTemplate;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class NotificationTemplateWithPlatformsDtoMapper
    extends AbstractConverter<NotificationTemplate, NotificationTemplateWithPlatformsDto> {

    @Override
    protected NotificationTemplateWithPlatformsDto convert(NotificationTemplate notificationTemplate) {
        return NotificationTemplateWithPlatformsDto.builder()
            .notificationTemplateMainInfoDto(NotificationTemplateMainInfoDto.builder()
                .type(notificationTemplate.getNotificationType())
                .trigger(notificationTemplate.getTrigger())
                .triggerDescription(notificationTemplate.getTrigger().getDescription())
                .triggerDescriptionEng(notificationTemplate.getTrigger().getDescriptionEng())
                .time(notificationTemplate.getTime())
                .timeDescription(notificationTemplate.getTime().getDescription())
                .timeDescriptionEng(notificationTemplate.getTime().getDescriptionEng())
                .schedule(notificationTemplate.getSchedule())
                .title(notificationTemplate.getTitle())
                .titleEng(notificationTemplate.getTitleEng())
                .notificationStatus(notificationTemplate.getNotificationStatus())
                .build())
            .platforms(notificationTemplate.getNotificationPlatforms().stream()
                .map(notificationPlatform -> NotificationPlatformDto.builder()
                    .receiverType(notificationPlatform.getNotificationReceiverType())
                    .nameEng(notificationPlatform
                        .getNotificationReceiverType()
                        .getName())
                    .body(notificationPlatform.getBody())
                    .bodyEng(notificationPlatform.getBodyEng())
                    .status(notificationPlatform.getNotificationStatus())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
