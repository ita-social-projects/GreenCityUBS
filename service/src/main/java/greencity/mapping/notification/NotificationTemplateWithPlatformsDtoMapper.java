package greencity.mapping.notification;

import greencity.dto.notification.NotificationPlatformDto;
import greencity.dto.notification.NotificationTemplateMainInfoDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsDto;
import greencity.entity.notifications.NotificationTemplate;
import java.util.Objects;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
class NotificationTemplateWithPlatformsDtoMapper
    extends AbstractConverter<NotificationTemplate, NotificationTemplateWithPlatformsDto> {
    @Override
    protected NotificationTemplateWithPlatformsDto convert(NotificationTemplate notificationTemplate) {
        return NotificationTemplateWithPlatformsDto.builder()
            .notificationTemplateMainInfoDto(NotificationTemplateMainInfoDto.builder()
                .type(notificationTemplate.getNotificationType())
                .trigger(notificationTemplate.getTrigger())
                .triggerDescription(notificationTemplate.getTrigger().getDescriptionUk())
                .triggerDescriptionEng(notificationTemplate.getTrigger().getDescriptionEn())
                .time(notificationTemplate.getTime())
                .timeDescription(notificationTemplate.getTime().getDescriptionUk())
                .timeDescriptionEng(notificationTemplate.getTime().getDescriptionEn())
                .schedule(notificationTemplate.getSchedule())
                .title(notificationTemplate.getTitleUk())
                .titleEng(notificationTemplate.getTitleEn())
                .notificationStatus(notificationTemplate.getNotificationStatus())
                .scheduleUpdateForbidden(notificationTemplate.isScheduleUpdateForbidden())
                .userCategoryDescription(
                    Objects.isNull(notificationTemplate.getUserCategory()) ? null
                        : notificationTemplate.getUserCategory().getDescriptionUk())
                .userCategoryDescriptionEng(Objects.isNull(notificationTemplate.getUserCategory()) ? null
                    : notificationTemplate.getUserCategory().getDescriptionEn())
                .build())
            .platforms(notificationTemplate.getNotificationPlatforms().stream()
                .map(platform -> NotificationPlatformDto.builder()
                    .id(platform.getId())
                    .receiverType(platform.getNotificationReceiverType())
                    .nameEng(platform
                        .getNotificationReceiverType()
                        .getName())
                    .body(platform.getBodyUk())
                    .bodyEng(platform.getBodyEn())
                    .status(platform.getNotificationStatus())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
