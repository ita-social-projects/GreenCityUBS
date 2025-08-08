package greencity.mapping.notification;

import greencity.dto.notification.NotificationPlatformDto;
import greencity.dto.notification.NotificationTemplateMainInfoDto;
import greencity.entity.notifications.NotificationPlatform;
import greencity.entity.notifications.NotificationTemplate;
import java.util.Objects;
import java.util.function.Function;

public class NotificationMappers {
    private NotificationMappers() {
    }

    public static final Function<NotificationTemplate, NotificationTemplateMainInfoDto> toNotificationTemplateMainInfoDto =
        notificationTemplate -> NotificationTemplateMainInfoDto.builder()
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
            .userCategoryDescriptionEn(
                Objects.isNull(notificationTemplate.getUserCategory()) ? null
                    : notificationTemplate.getUserCategory().getDescriptionEn())
            .build();

    public static final Function<NotificationPlatform, NotificationPlatformDto> toNotificationPlatformDto =
        platform -> NotificationPlatformDto.builder()
            .id(platform.getId())
            .receiverType(platform.getNotificationReceiverType())
            .nameEn(platform.getNotificationReceiverType().getName())
            .bodyUk(platform.getBodyUk())
            .bodyEn(platform.getBodyEn())
            .status(platform.getNotificationStatus())
            .build();
}
