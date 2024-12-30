package greencity.repository;

import greencity.entity.notifications.NotificationParameter;
import greencity.entity.notifications.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface NotificationParameterRepository extends JpaRepository<NotificationParameter, Long> {
    /**
     * Finds all notification parameters by user notification.
     *
     * @param userNotification User notification.
     * @return set of notification parameters.
     *
     *         author Vladyslav Haliara
     */
    Optional<Set<NotificationParameter>> findNotificationParameterByUserNotification(UserNotification userNotification);
}
