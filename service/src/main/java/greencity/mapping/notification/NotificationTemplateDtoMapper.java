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
                /*.platforms(notificationTemplate.getNotificationPlatforms().stream()
                        .map(notificationPlatform -> NotificationPlatformDto.builder()
                                .receiverType(notificationPlatform.getNotificationReceiverType())
                                .nameEng(notificationPlatform
                                        .getNotificationReceiverType()
                                        .getName())
                                .body(notificationPlatform.getBody())
                                .bodyEng(notificationPlatform.getBodyEng())
                                .status(notificationPlatform.getNotificationStatus())
                                .build())
                        .collect(Collectors.toList()))*/
                .build();
    }
}
