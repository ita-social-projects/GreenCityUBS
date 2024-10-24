package greencity.repository;

import greencity.enums.NotificationType;
import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
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
     * The method returns last notification by {@link NotificationType} and orderId.
     *
     * @return {@link Optional} of {@link UserNotification}.
     */
    Optional<UserNotification> findFirstByOrderIdAndNotificationTypeInOrderByNotificationTimeDesc(Long orderId,
        NotificationType... notificationType);

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

    /**
     * Changes {@link UserNotification} `read` as true.
     *
     * @param notificationId to change
     */
    @Transactional
    @Modifying
    @Query("UPDATE UserNotification n SET n.read = true WHERE n.id = :notificationId")
    void markNotificationAsViewed(Long notificationId);

    /**
     * Changes {@link UserNotification} `viewed` as false.
     *
     * @param notificationId to change
     */
    @Transactional
    @Modifying
    @Query("UPDATE UserNotification n SET n.read = false WHERE n.id = :notificationId")
    void markNotificationAsNotViewed(Long notificationId);

    /**
     * Method to delete specific Notification.
     *
     * @param notificationId id of searched Notification
     * @param userId         id of user
     */
    void deleteUserNotificationByIdAndUserId(Long notificationId, Long userId);

    /**
     * Checks if a notification with the specified ID exists for the specified user.
     *
     * @param notificationId the ID of the notification to check
     * @param userId         the ID of the user for whom the notification belongs
     * @return true if the notification with the specified ID exists for the user,
     *         false otherwise
     */
    boolean existsByIdAndUserId(Long notificationId, Long userId);
}
