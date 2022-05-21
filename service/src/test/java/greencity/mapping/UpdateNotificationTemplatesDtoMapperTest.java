package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.notification.UpdateNotificationTemplatesDto;
import greencity.entity.notifications.NotificationTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateNotificationTemplatesDtoMapperTest {
    @InjectMocks
    private UpdateNotificationTemplatesDtoMapper updateNotificationTemplatesDtoMapper;

    @Test
    void convert() {
        UpdateNotificationTemplatesDto expected = ModelUtils.getUpdateNotificationTemplatesDto();
        NotificationTemplate notificationTemplate = ModelUtils.getNotificationTemplate();

        Assertions.assertEquals(expected.getNotificationType(),
            updateNotificationTemplatesDtoMapper.convert(notificationTemplate).getNotificationType());
        Assertions.assertEquals(expected.getBody(),
            updateNotificationTemplatesDtoMapper.convert(notificationTemplate).getBody());
        Assertions.assertEquals(expected.getLanguageId(),
            updateNotificationTemplatesDtoMapper.convert(notificationTemplate).getLanguageId());
    }
}
