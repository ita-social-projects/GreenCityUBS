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
public class AddNotificationTemplateWithPlatformsDtoMapperTest {
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
        assertEquals(dto.getTitle(), notification.getTitle());
        assertEquals(dto.getTitleEng(), notification.getTitleEng());
        assertEquals(dto.getUserCategory(), notification.getUserCategory());

        assertEquals(platformDto.getNotificationReceiverType(), platform.getNotificationReceiverType());
        assertEquals(platformDto.getBody(), platform.getBody());
        assertEquals(platformDto.getBodyEng(), platform.getBodyEng());
    }
}
