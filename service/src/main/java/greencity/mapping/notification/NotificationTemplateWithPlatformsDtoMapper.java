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
                .triggerDescriptionUk(notificationTemplate.getTrigger().getDescriptionUk())
                .triggerDescriptionEn(notificationTemplate.getTrigger().getDescriptionEn())
                .time(notificationTemplate.getTime())
                .timeDescriptionUk(notificationTemplate.getTime().getDescriptionUk())
                .timeDescriptionEn(notificationTemplate.getTime().getDescriptionEn())
                .schedule(notificationTemplate.getSchedule())
                .titleUk(notificationTemplate.getTitleUk())
                .titleEn(notificationTemplate.getTitleEn())
                .notificationStatus(notificationTemplate.getNotificationStatus())
                .scheduleUpdateForbidden(notificationTemplate.isScheduleUpdateForbidden())
                .userCategoryDescriptionUk(
                    Objects.isNull(notificationTemplate.getUserCategory()) ? null
                        : notificationTemplate.getUserCategory().getDescriptionUk())
                .userCategoryDescriptionEn(Objects.isNull(notificationTemplate.getUserCategory()) ? null
                    : notificationTemplate.getUserCategory().getDescriptionEn())
                .build())
            .platforms(notificationTemplate.getNotificationPlatforms().stream()
                .map(platform -> NotificationPlatformDto.builder()
                    .id(platform.getId())
                    .receiverType(platform.getNotificationReceiverType())
                    .nameEn(platform
                        .getNotificationReceiverType()
                        .getName())
                    .bodyUk(platform.getBodyUk())
                    .bodyEn(platform.getBodyEn())
                    .status(platform.getNotificationStatus())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
