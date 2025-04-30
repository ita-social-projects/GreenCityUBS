package greencity.mapping.notification;

import greencity.dto.notification.AddNotificationPlatformDto;
import greencity.dto.notification.AddNotificationTemplateWithPlatformsDto;
import greencity.entity.notifications.NotificationPlatform;
import greencity.entity.notifications.NotificationTemplate;
import greencity.enums.NotificationStatus;
import greencity.enums.NotificationTime;
import greencity.enums.NotificationTrigger;
import greencity.enums.NotificationType;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class AddNotificationTemplateWithPlatformsDtoMapper
    extends AbstractConverter<AddNotificationTemplateWithPlatformsDto, NotificationTemplate> {
    @Override
    protected NotificationTemplate convert(
        AddNotificationTemplateWithPlatformsDto template) {
        var notificationTemplate = NotificationTemplate
            .builder()
            .title(template.getTitle())
            .titleEng(template.getTitleEng())
            .schedule(template.getSchedule())
            .notificationType(NotificationType.CUSTOM)
            .trigger(NotificationTrigger.CUSTOM)
            .userCategory(template.getUserCategory())
            .time(NotificationTime.IMMEDIATELY)
            .notificationStatus(NotificationStatus.ACTIVE)
            .isScheduleUpdateForbidden(false)
            .build();

        notificationTemplate.addPlatforms(convertPlatforms(template.getPlatforms()));
        return notificationTemplate;
    }

    private List<NotificationPlatform> convertPlatforms(List<AddNotificationPlatformDto> platforms) {
        return Objects.isNull(platforms) ? Collections.emptyList()
            : platforms.stream()
                .map(this::convertPlatform)
                .toList();
    }

    private NotificationPlatform convertPlatform(
        AddNotificationPlatformDto platform) {
        return NotificationPlatform.builder()
            .body(platform.getBody())
            .bodyEng(platform.getBodyEng())
            .notificationReceiverType(platform.getNotificationReceiverType())
            .notificationStatus(NotificationStatus.ACTIVE)
            .build();
    }
}
