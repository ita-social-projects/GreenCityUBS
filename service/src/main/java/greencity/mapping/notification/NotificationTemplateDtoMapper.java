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
                    .triggerDescription(notificationTemplate.getTrigger().getDescription())
                    .triggerDescriptionEng(notificationTemplate.getTrigger().getDescriptionEng())
                    .time(notificationTemplate.getTime())
                    .timeDescription(notificationTemplate.getTime().getDescription())
                    .timeDescriptionEng(notificationTemplate.getTime().getDescriptionEng())
                    .schedule(notificationTemplate.getSchedule())
                    .title(notificationTemplate.getTitle())
                    .titleEng(notificationTemplate.getTitleEng())
                    .notificationStatus(notificationTemplate.getNotificationStatus())
                    .userCategoryDescription(Objects.isNull(notificationTemplate.getUserCategory()) ? null
                        : notificationTemplate.getUserCategory().getDescription())
                    .userCategoryDescriptionEng(Objects.isNull(notificationTemplate.getUserCategory()) ? null
                        : notificationTemplate.getUserCategory().getDescriptionEng())
                    .build())
            .build();
    }
}
