package greencity.mapping.notification;

import greencity.ModelUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateWithPlatformsUpdateDtoMapperTest {
    @InjectMocks
    private NotificationTemplateWithPlatformsUpdateDtoMapper notificationTemplateWithPlatformsDtoMapper;

    @Test
    void convert() {
        var notification = ModelUtils.TEST_NOTIFICATION_TEMPLATE;
        var platform = notification.getNotificationPlatforms().getFirst();

        var dto = notificationTemplateWithPlatformsDtoMapper.convert(notification);
        var mainInfoDto = dto.getNotificationTemplateUpdateInfo();
        var platformDto = dto.getPlatforms().getFirst();

        assertEquals(notification.getNotificationType(), mainInfoDto.getType());
        assertEquals(notification.getTrigger(), mainInfoDto.getTrigger());
        assertEquals(notification.getTime(), mainInfoDto.getTime());
        assertEquals(notification.getSchedule(), mainInfoDto.getSchedule());
        assertEquals(notification.getTitleUk(), mainInfoDto.getTitleUk());
        assertEquals(notification.getTitleEn(), mainInfoDto.getTitleEn());
        assertEquals(notification.getUserCategory(), mainInfoDto.getUserCategory());

        assertEquals(platform.getId(), platformDto.getId());
        assertEquals(platform.getNotificationReceiverType(), platformDto.getReceiverType());
        assertEquals(platform.getNotificationReceiverType().getName(), platformDto.getNameEn());
        assertEquals(platform.getBodyUk(), platformDto.getBodyUk());
        assertEquals(platform.getBodyEn(), platformDto.getBodyEn());
        assertEquals(platform.getNotificationStatus(), platformDto.getStatus());
    }
}
