package greencity.mapping.notification;

import greencity.ModelUtils;
import greencity.dto.notification.NotificationPlatformDto;
import greencity.dto.notification.NotificationTemplateMainInfoDto;
import greencity.entity.notifications.NotificationPlatform;
import greencity.entity.notifications.NotificationTemplate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationMappersTest {
    @Test
    void toNotificationTemplateMainInfoDto_AllFields() {
        NotificationTemplate notification = ModelUtils.TEST_NOTIFICATION_TEMPLATE;

        NotificationTemplateMainInfoDto dto =
            NotificationMappers.toNotificationTemplateMainInfoDto(notification);

        assertEquals(notification.getNotificationType(), dto.getType());
        assertEquals(notification.getTrigger(), dto.getTrigger());
        assertEquals(notification.getTrigger().getDescriptionUk(), dto.getTriggerDescriptionUk());
        assertEquals(notification.getTrigger().getDescriptionEn(), dto.getTriggerDescriptionEn());
        assertEquals(notification.getTime(), dto.getTime());
        assertEquals(notification.getTime().getDescriptionUk(), dto.getTimeDescriptionUk());
        assertEquals(notification.getTime().getDescriptionEn(), dto.getTimeDescriptionEn());
        assertEquals(notification.getSchedule(), dto.getSchedule());
        assertEquals(notification.getTitleUk(), dto.getTitleUk());
        assertEquals(notification.getTitleEn(), dto.getTitleEn());
        assertEquals(notification.getNotificationStatus(), dto.getNotificationStatus());
        assertEquals(notification.getUserCategory().getDescriptionUk(), dto.getUserCategoryDescriptionUk());
        assertEquals(notification.getUserCategory().getDescriptionEn(), dto.getUserCategoryDescriptionEn());
    }

    @Test
    void toNotificationPlatformDto_AllFields() {
        NotificationPlatform platform = ModelUtils.TEST_NOTIFICATION_TEMPLATE
            .getNotificationPlatforms().getFirst();

        NotificationPlatformDto dto =
            NotificationMappers.toNotificationPlatformDto(platform);

        assertEquals(platform.getId(), dto.getId());
        assertEquals(platform.getNotificationReceiverType(), dto.getReceiverType());
        assertEquals(platform.getNotificationReceiverType().getName(), dto.getNameEn());
        assertEquals(platform.getBodyUk(), dto.getBodyUk());
        assertEquals(platform.getBodyEn(), dto.getBodyEn());
        assertEquals(platform.getNotificationStatus(), dto.getStatus());
    }
}
