package greencity.repository;

import greencity.enums.NotificationType;
import greencity.entity.schedule.NotificationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationScheduleRepo extends JpaRepository<NotificationSchedule, NotificationType> {
    /**
     * method, that returns {@link NotificationSchedule} by
     * {@link NotificationType}.
     *
     * @param notificationType .
     * @return {@link NotificationSchedule}
     * @author Max Nazaruk
     */
    NotificationSchedule findNotificationScheduleByNotificationType(NotificationType notificationType);
}
