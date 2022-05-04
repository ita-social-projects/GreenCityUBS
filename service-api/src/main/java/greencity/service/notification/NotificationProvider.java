package greencity.service.notification;

import greencity.entity.notifications.UserNotification;

public interface NotificationProvider {
    /**
     * Sends notification to user.
     *
     * @param notification {@link UserNotification}
     */
    void sendNotification(UserNotification notification);
}
