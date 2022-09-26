package greencity.repository;

import greencity.enums.NotificationType;
import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    /**
     * The method returns all notifications for user.
     *
     * @return list of {@link UserNotification}.
     */
    Page<UserNotification> findAllByUser(User user, Pageable pageable);

    /**
     * The method returns last notification by User and Type.
     *
     * @return {@link Optional} of {@link UserNotification}.
     */
    Optional<UserNotification> findTop1UserNotificationByUserAndNotificationTypeOrderByNotificationTimeDesc(User user,
        NotificationType type);

    /**
     * The method returns last notification by {@link NotificationType} and
     * orderNumber from
     * {@link greencity.entity.notifications.NotificationParameter}.
     *
     * @return {@link Optional} of {@link UserNotification}.
     */
    @Query(nativeQuery = true, value = "select * from user_notifications "
        + "join notification_parameters np on user_notifications.id = np.notification_id "
        + "where notification_type = :type and np.key = 'orderNumber' and np.value = :orderNumber "
        + "order by notification_time desc "
        + "limit 1;")
    Optional<UserNotification> findLastNotificationByNotificationTypeAndOrderNumber(String type, String orderNumber);

    /**
     * Method that returns amount unread notifications.
     *
     * @author Ihor Volianskyi
     */
    long countUserNotificationByUserAndReadIsFalse(User user);

    /**
     * Method that returns list of user ids by {@link NotificationType} and
     * {@link LocalDate}.
     *
     * @return {@link List} of {@link Long}.
     */
    @Query(nativeQuery = true, value = "SELECT distinct users_id FROM user_notifications "
        + "WHERE CAST(notification_time AS DATE) > :dateOfLastNotification AND "
        + "notification_type = :type")
    List<Long> getUserIdByDateOfLastNotificationAndNotificationType(LocalDate dateOfLastNotification, String type);
}
