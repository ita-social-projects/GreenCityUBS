package greencity.mapping.notification;

import greencity.dto.notification.NotificationPlatformDto;
import greencity.dto.notification.NotificationTemplateMainInfoDto;
import greencity.entity.notifications.NotificationPlatform;
import greencity.entity.notifications.NotificationTemplate;
import java.util.Objects;

/**
 * Utility class providing mapping methods for converting notification-related
 * entities into their corresponding DTO representations.
 * <p>
 * This class is non-instantiable and exposes all mappers as public static
 * methods, allowing them to be used directly in services, mappers, or stream
 * operations.
 * </p>
 *
 * <h2>Available Mappers</h2>
 * <ul>
 * <li>{@link #toNotificationTemplateMainInfoDto(NotificationTemplate)} – Maps a
 * {@link NotificationTemplate} entity to a
 * {@link NotificationTemplateMainInfoDto}.</li>
 * <li>{@link #toNotificationPlatformDto(NotificationPlatform)} – Maps a
 * {@link NotificationPlatform} entity to a
 * {@link NotificationPlatformDto}.</li>
 * </ul>
 */
public class NotificationMappers {
    /**
     * Private constructor to prevent instantiation.
     */
    private NotificationMappers() {
    }

    /**
     * Converts a {@link NotificationTemplate} entity to a
     * {@link NotificationTemplateMainInfoDto}.
     * <p>
     * Maps the notification type, trigger and its descriptions, time and its
     * descriptions, schedule, titles, status, schedule update restriction flag, and
     * user category descriptions (if available).
     * </p>
     *
     * @param notificationTemplate the entity to map
     * @return a populated {@link NotificationTemplateMainInfoDto}
     */
    public static NotificationTemplateMainInfoDto toNotificationTemplateMainInfoDto(
        NotificationTemplate notificationTemplate) {
        return NotificationTemplateMainInfoDto.builder()
            .type(notificationTemplate.getNotificationType())
            .trigger(notificationTemplate.getTrigger())
            .triggerDescriptionUk(notificationTemplate.getTrigger().getDescriptionUk())
            .triggerDescriptionEn(notificationTemplate.getTrigger().getDescriptionEn())
            .time(notificationTemplate.getTime())
            .timeDescriptionUk(notificationTemplate.getTime().getDescriptionUk())
            .timeDescriptionEn(notificationTemplate.getTime().getDescriptionEn())
            .schedule(notificationTemplate.getSchedule())
            .titleUk(notificationTemplate.getTitleUk())
            .titleEn(notificationTemplate.getTitleEn())
            .notificationStatus(notificationTemplate.getNotificationStatus())
            .scheduleUpdateForbidden(notificationTemplate.isScheduleUpdateForbidden())
            .userCategoryDescriptionUk(
                Objects.isNull(notificationTemplate.getUserCategory()) ? null
                    : notificationTemplate.getUserCategory().getDescriptionUk())
            .userCategoryDescriptionEn(
                Objects.isNull(notificationTemplate.getUserCategory()) ? null
                    : notificationTemplate.getUserCategory().getDescriptionEn())
            .build();
    }

    /**
     * Converts a {@link NotificationPlatform} entity to a
     * {@link NotificationPlatformDto}.
     * <p>
     * Maps the platform ID, receiver type, receiver type name (English), message
     * body in both languages, and notification status.
     * </p>
     *
     * @param notificationPlatform the entity to map
     * @return a populated {@link NotificationPlatformDto}
     */
    public static NotificationPlatformDto toNotificationPlatformDto(NotificationPlatform notificationPlatform) {
        return NotificationPlatformDto.builder()
            .id(notificationPlatform.getId())
            .receiverType(notificationPlatform.getNotificationReceiverType())
            .nameEn(notificationPlatform.getNotificationReceiverType().getName())
            .bodyUk(notificationPlatform.getBodyUk())
            .bodyEn(notificationPlatform.getBodyEn())
            .status(notificationPlatform.getNotificationStatus())
            .build();
    }
}
