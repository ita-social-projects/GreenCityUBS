package greencity.repository;

import greencity.entity.notifications.NotificationParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationParameterRepository extends JpaRepository<NotificationParameter, Long> {
    /**
     * {@inheritDoc}
     */
    List<NotificationParameter> findAll();
}
