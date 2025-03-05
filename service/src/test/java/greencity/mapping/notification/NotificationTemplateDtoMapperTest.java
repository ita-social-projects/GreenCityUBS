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
        assertEquals(notification.getTrigger().getDescriptionUk(), mainInfoDto.getTriggerDescription());
        assertEquals(notification.getTrigger().getDescriptionEn(), mainInfoDto.getTriggerDescriptionEng());
        assertEquals(notification.getTime(), mainInfoDto.getTime());
        assertEquals(notification.getTime().getDescriptionUk(), mainInfoDto.getTimeDescription());
        assertEquals(notification.getTime().getDescriptionEn(), mainInfoDto.getTimeDescriptionEng());
        assertEquals(notification.getSchedule(), mainInfoDto.getSchedule());
        assertEquals(notification.getTitleUk(), mainInfoDto.getTitle());
        assertEquals(notification.getTitleEn(), mainInfoDto.getTitleEng());
        assertEquals(notification.getNotificationStatus(), mainInfoDto.getNotificationStatus());
        assertEquals(notification.getUserCategory().getDescriptionUk(), mainInfoDto.getUserCategoryDescription());
        assertEquals(notification.getUserCategory().getDescriptionEn(), mainInfoDto.getUserCategoryDescriptionEng());
    }
}
