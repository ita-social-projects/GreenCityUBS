package greencity.repository;

import greencity.entity.enums.NotificationReceiverType;
import greencity.entity.enums.NotificationType;
import greencity.entity.notifications.NotificationTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    Optional<NotificationTemplate> findNotificationTemplateByNotificationTypeAndLanguageCodeAndNotificationReceiverType(
        NotificationType type, String languageCode, NotificationReceiverType receiverType);

    /**
     * Method, that returns {@link Optional} of {@link NotificationTemplate} by id.
     *
     *
     * @return {@link Optional} of {@link NotificationTemplate}.
     * @author Dima Sannytski
     */
    Optional<NotificationTemplate> findNotificationTemplateById(Long id);

    /**
     * Method that returns all notification templates.
     *
     * @author Dima Sannytski
     */
    Page<NotificationTemplate> findAll(Pageable pageable);

    /**
     * Method that updates body in notification templates for receiving type SITE.
     *
     * @author Natalia Kozak
     */
    @Transactional
    @Modifying
    @Query(value = "update notification_templates set body = :body where notification_type = :notificationType "
        + "and notification_receiver_type = 'SITE' and language_id = :languageId",
        nativeQuery = true)
    void updateNotificationTemplateForSITE(@Param("body") String body,
        @Param("notificationType") String notificationType,
        @Param("languageId") long languageId);

    /**
     * Method that updates body in notification templates for receiving type OTHER.
     *
     * @author Natalia Kozak
     */
    @Transactional
    @Modifying
    @Query(value = "update notification_templates set body = :body where notification_type = :notificationType "
        + "and notification_receiver_type = 'OTHER' and language_id = :languageId",
        nativeQuery = true)
    void updateNotificationTemplateForOTHER(@Param("body") String body,
        @Param("notificationType") String notificationType,
        @Param("languageId") long languageId);
}