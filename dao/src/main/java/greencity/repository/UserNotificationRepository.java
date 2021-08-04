package greencity.repository;

import greencity.entity.enums.NotificationType;
import greencity.entity.notifications.UserNotification;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    /**
     * The method returns all orders for user.
     *
     * @return list of {@link UserNotification}.
     */
    List<UserNotification> findAllByUser(User user);

    /**
     * The method returns last notification by User and Type.
     *
     * @return {@link Optional} of {@link UserNotification}.
     */
    Optional<UserNotification> findTop1UserNotificationByUserAndNotificationTypeOrderByNotificationTimeDesc(User user,
        NotificationType type);

    /**
     * The method returns last notification by {@link NotificationType}
     * and orderNumber from {@link greencity.entity.notifications.NotificationParameter}.
     *
     * @return {@link Optional} of {@link UserNotification}.
     */
    @Query(nativeQuery = true, value = "select * from user_notifications "
            + "join notification_parameters np on user_notifications.id = np.notification_id "
            + "where notification_type = :type and np.key = 'orderNumber' and np.value = :orderNumber "
            + "order by notification_time desc "
            + "limit 1;")
    Optional<UserNotification> findLastNotificationByNotificationTypeAndOrderNumber(String type, String orderNumber);
}
