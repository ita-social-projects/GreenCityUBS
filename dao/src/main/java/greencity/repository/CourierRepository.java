package greencity.repository;

import greencity.entity.order.Courier;
import greencity.entity.user.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    /**
     * {@inheritDoc}
     */
    Courier findCourierByLocation(Location location);
}
