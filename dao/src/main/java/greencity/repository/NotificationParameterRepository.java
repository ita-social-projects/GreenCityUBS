package greencity.repository;

import greencity.entity.notifications.NotificationParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationParameterRepository extends JpaRepository<NotificationParameter, Long> {
}
