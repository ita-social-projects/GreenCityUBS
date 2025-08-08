package greencity.mapping.notification;

import greencity.dto.notification.NotificationPlatformDto;
import greencity.dto.notification.NotificationTemplateMainInfoDto;
import greencity.entity.notifications.NotificationPlatform;
import greencity.entity.notifications.NotificationTemplate;
import java.util.Objects;
import java.util.function.Function;

/**
 * Utility class providing reusable mapping functions for converting
 * notification-related entity objects into their corresponding DTO
 * representations.
 * <p>
 * This class is not meant to be instantiated; all mappings are exposed as
 * public static {@link Function} instances for direct usage in mappers,
 * services, or stream pipelines.
 * </p>
 *
 * <h2>Available Mappers</h2>
 * <ul>
 * <li>{@link #toNotificationTemplateMainInfoDto} – Maps a
 * {@link NotificationTemplate} entity to a
 * {@link NotificationTemplateMainInfoDto}.</li>
 * <li>{@link #toNotificationPlatformDto} – Maps a {@link NotificationPlatform}
 * entity to a {@link NotificationPlatformDto}.</li>
 * </ul>
 */
public class NotificationMappers {
    /**
     * Private constructor to prevent instantiation.
     */
    private NotificationMappers() {
    }

    /**
     * Maps a {@link NotificationTemplate} entity to a
     * {@link NotificationTemplateMainInfoDto}.
     * <p>
     * Copies type, trigger details, time details, schedule, title, status, schedule
     * update restriction flag, and user category descriptions.
     * </p>
     *
     * @see NotificationTemplate
     * @see NotificationTemplateMainInfoDto
     */
    public static final Function<NotificationTemplate, NotificationTemplateMainInfoDto> toNotificationTemplateMainInfoDto =
        notificationTemplate -> NotificationTemplateMainInfoDto.builder()
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

    /**
     * Maps a {@link NotificationPlatform} entity to a
     * {@link NotificationPlatformDto}.
     * <p>
     * Copies platform ID, receiver type, English name, body texts in both
     * languages, and notification status.
     * </p>
     *
     * @see NotificationPlatform
     * @see NotificationPlatformDto
     */
    public static final Function<NotificationPlatform, NotificationPlatformDto> toNotificationPlatformDto =
        platform -> NotificationPlatformDto.builder()
            .id(platform.getId())
            .receiverType(platform.getNotificationReceiverType())
            .nameEn(platform.getNotificationReceiverType().getName())
            .bodyUk(platform.getBodyUk())
            .bodyEn(platform.getBodyEn())
            .status(platform.getNotificationStatus())
            .build();
}
