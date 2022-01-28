package greencity.repository;

import greencity.entity.enums.NotificationType;
import greencity.entity.schedule.NotificationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationScheduleRepo extends JpaRepository<NotificationSchedule, NotificationType> {
}
