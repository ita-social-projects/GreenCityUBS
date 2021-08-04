package greencity.repository;

import greencity.entity.enums.NotificationType;
import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    List<UserNotification> findAllByUser(User user);

    Optional<UserNotification> findTop1UserNotificationByUserAndNotificationTypeOrderByNotificationTimeDesc(User user,
        NotificationType type);

    @Query(nativeQuery = true, value = "select * from user_notifications " +
        "join notification_parameters np on user_notifications.id = np.notification_id " +
        "where notification_type = :type and np.key = 'orderNumber' and np.value = :orderNumber " +
        "order by notification_time desc " +
        "limit 1;")
    Optional<UserNotification> findLastNotificationByNotificationTypeAndOrderNumber(String type, String orderNumber);

}
