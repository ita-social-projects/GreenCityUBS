package greencity.mapping.notification;

import greencity.ModelUtils;
import greencity.dto.notification.NotificationTemplateLocalizedDto;
import greencity.entity.notifications.NotificationTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateLocalizedDtoMapperTest {
    @InjectMocks
    private NotificationTemplateLocalizedDtoMapper notificationTemplateLocalizedDtoMapper;

    @Test
    void convert() {
        NotificationTemplateLocalizedDto expected = ModelUtils.getNotificationTemplateLocalizeDto();
        NotificationTemplate notificationTemplate = ModelUtils.getNotificationTemplate();

        Assertions.assertEquals(expected.getNotificationType(),
            notificationTemplateLocalizedDtoMapper.convert(notificationTemplate).getNotificationType());
        Assertions.assertEquals(expected.getId(),
            notificationTemplateLocalizedDtoMapper.convert(notificationTemplate).getId());
    }
}