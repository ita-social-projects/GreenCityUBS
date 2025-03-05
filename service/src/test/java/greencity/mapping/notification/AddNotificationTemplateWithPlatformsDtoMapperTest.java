package greencity.mapping.notification;

import greencity.ModelUtils;
import greencity.enums.NotificationTime;
import greencity.enums.NotificationTrigger;
import greencity.enums.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AddNotificationTemplateWithPlatformsDtoMapperTest {
    @InjectMocks
    private AddNotificationTemplateWithPlatformsDtoMapper mapper;

    @Test
    void convertTest() {
        var dto = ModelUtils.createAddNotificationTemplateWithPlatforms();
        var platformDto = dto.getPlatforms().getFirst();

        var notification = mapper.convert(dto);
        var platform = notification.getNotificationPlatforms().getFirst();

        assertEquals(NotificationType.CUSTOM, notification.getNotificationType());
        assertEquals(NotificationTrigger.CUSTOM, notification.getTrigger());
        assertEquals(NotificationTime.IMMEDIATELY, notification.getTime());
        assertEquals(dto.getSchedule(), notification.getSchedule());
        assertEquals(dto.getTitle(), notification.getTitleUk());
        assertEquals(dto.getTitleEng(), notification.getTitleEn());
        assertEquals(dto.getUserCategory(), notification.getUserCategory());

        assertEquals(platformDto.getNotificationReceiverType(), platform.getNotificationReceiverType());
        assertEquals(platformDto.getBody(), platform.getBodyUk());
        assertEquals(platformDto.getBodyEng(), platform.getBodyEn());
    }
}
