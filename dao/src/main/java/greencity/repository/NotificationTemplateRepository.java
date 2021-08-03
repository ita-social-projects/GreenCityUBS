package greencity.repository;

import greencity.entity.enums.NotificationType;
import greencity.entity.language.Language;
import greencity.entity.notifications.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findNotificationTemplateByNotificationTypeAndLanguageCode(
            NotificationType type, String languageCode);
}
