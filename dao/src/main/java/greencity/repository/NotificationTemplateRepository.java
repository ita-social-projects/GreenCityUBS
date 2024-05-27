package greencity.repository;

import greencity.entity.notifications.NotificationTemplate;
import greencity.enums.NotificationReceiverType;
import greencity.enums.NotificationType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    /**
     * method, that returns {@link Optional}of{@link NotificationTemplate} by Type
     * and LanguageCode and Receiver Type.
     *
     *
     * @return {@link Optional} of {@link NotificationTemplate} with all codes.
     * @author Ann Sakhno
     */
    @Query("select nt from NotificationTemplate nt inner join fetch nt.notificationPlatforms as np "
        + "where nt.notificationType = :type and np.notificationReceiverType = :receiverType")
    Optional<NotificationTemplate> findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
        @Param(value = "type") NotificationType type,
        @Param(value = "receiverType") NotificationReceiverType receiverType);

    /**
     * method, that finds schedule of active Notification by Notification type.
     *
     * @return {@link String}
     * @author Denys Ryhal
     */
    @Query("SELECT t.schedule FROM NotificationTemplate t "
        + "WHERE t.notificationType = :type AND t.notificationStatus = 'ACTIVE' "
        + "ORDER BY t.id DESC "
        + "LIMIT 1")
    String findScheduleOfActiveTemplateByType(@Param(value = "type") NotificationType type);

    @Query("SELECT t FROM NotificationTemplate t "
        + "WHERE t.notificationType = 'CUSTOM' AND t.notificationStatus = 'ACTIVE'")
    List<NotificationTemplate> findAllActiveCustomNotificationsTemplates();

    @Query("select nt from NotificationTemplate nt inner join fetch nt.notificationPlatforms as np "
        + "where nt.id = :templateId and np.notificationReceiverType = :receiverType")
    Optional<NotificationTemplate> findNotificationTemplateByIdAndNotificationReceiverType(
        @Param(value = "templateId") Long templateId,
        @Param(value = "receiverType") NotificationReceiverType receiverType);
}