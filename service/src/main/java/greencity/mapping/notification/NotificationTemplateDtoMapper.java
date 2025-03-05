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
                    .triggerDescription(notificationTemplate.getTrigger().getDescriptionUk())
                    .triggerDescriptionEng(notificationTemplate.getTrigger().getDescriptionEn())
                    .time(notificationTemplate.getTime())
                    .timeDescription(notificationTemplate.getTime().getDescriptionUk())
                    .timeDescriptionEng(notificationTemplate.getTime().getDescriptionEn())
                    .schedule(notificationTemplate.getSchedule())
                    .title(notificationTemplate.getTitleUk())
                    .titleEng(notificationTemplate.getTitleEn())
                    .notificationStatus(notificationTemplate.getNotificationStatus())
                    .userCategoryDescription(Objects.isNull(notificationTemplate.getUserCategory()) ? null
                        : notificationTemplate.getUserCategory().getDescriptionUk())
                    .userCategoryDescriptionEng(Objects.isNull(notificationTemplate.getUserCategory()) ? null
                        : notificationTemplate.getUserCategory().getDescriptionEn())
                    .build())
            .build();
    }
}
