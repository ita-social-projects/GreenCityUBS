package greencity.repository;

import greencity.entity.order.Service;
import greencity.entity.user.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    /**
     * {@inheritDoc}
     */
    Service findByLocation(Location location);
}
