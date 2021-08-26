package greencity.repository;

import greencity.entity.enums.NotificationType;
import greencity.entity.notifications.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    /**
     * method, that returns {@link Optional}of{@link NotificationTemplate} by Type
     * and LanguageCode.
     *
     *
     * @return {@link Optional} of {@link NotificationTemplate} with all codes.
     * @author Ann Sakhno
     */
    Optional<NotificationTemplate> findNotificationTemplateByNotificationTypeAndLanguageCode(
        NotificationType type, String languageCode);
}