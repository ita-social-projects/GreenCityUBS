package greencity.repository;

import greencity.entity.notifications.NotificationParameter;
import greencity.entity.notifications.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface NotificationParameterRepository extends JpaRepository<NotificationParameter, Long> {
    Optional<Set<NotificationParameter>> findNotificationParameterByUserNotification(UserNotification userNotification);
}
