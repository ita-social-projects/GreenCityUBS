package greencity.mapping.notification;

import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.notification.NotificationTemplateMainInfoDto;
import greencity.entity.notifications.NotificationTemplate;
import java.util.Objects;
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
                NotificationTemplateMainInfoDto.builder()
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
                    .userCategoryDescriptionUk(Objects.isNull(notificationTemplate.getUserCategory()) ? null
                        : notificationTemplate.getUserCategory().getDescriptionUk())
                    .userCategoryDescriptionEn(Objects.isNull(notificationTemplate.getUserCategory()) ? null
                        : notificationTemplate.getUserCategory().getDescriptionEn())
                    .build())
            .build();
    }
}
