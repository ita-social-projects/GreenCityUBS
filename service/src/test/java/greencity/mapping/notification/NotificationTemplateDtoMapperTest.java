package greencity.mapping.notification;

import greencity.ModelUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateDtoMapperTest {

    @InjectMocks
    private NotificationTemplateDtoMapper notificationTemplateDtoMapper;

    @Test
    void convert() {
        var notification = ModelUtils.TEST_NOTIFICATION_TEMPLATE;

        var dto = notificationTemplateDtoMapper.convert(notification);
        var mainInfoDto = dto.getNotificationTemplateMainInfoDto();

        assertEquals(notification.getId(), dto.getId());
        assertEquals(notification.getNotificationType(), mainInfoDto.getType());
        assertEquals(notification.getTrigger(), mainInfoDto.getTrigger());
        assertEquals(notification.getTrigger().getDescription(), mainInfoDto.getTriggerDescription());
        assertEquals(notification.getTrigger().getDescriptionEng(), mainInfoDto.getTriggerDescriptionEng());
        assertEquals(notification.getTime(), mainInfoDto.getTime());
        assertEquals(notification.getTime().getDescription(), mainInfoDto.getTimeDescription());
        assertEquals(notification.getTime().getDescriptionEng(), mainInfoDto.getTimeDescriptionEng());
        assertEquals(notification.getSchedule(), mainInfoDto.getSchedule());
        assertEquals(notification.getTitle(), mainInfoDto.getTitle());
        assertEquals(notification.getTitleEng(), mainInfoDto.getTitleEng());
        assertEquals(notification.getNotificationStatus(), mainInfoDto.getNotificationStatus());
        assertEquals(notification.getUserCategory().getDescription(), mainInfoDto.getUserCategoryDescription());
        assertEquals(notification.getUserCategory().getDescriptionEng(), mainInfoDto.getUserCategoryDescriptionEng());
    }
}
