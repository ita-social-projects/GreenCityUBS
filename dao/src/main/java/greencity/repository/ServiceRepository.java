package greencity.repository;

import greencity.entity.order.Service;
import greencity.entity.user.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    /**
     * {@inheritDoc}
     */
    Service findByLocation(Location location);

    /**
     * Method that return Service.
     * 
     * @param orderId   {@link Long}
     * @param courierId {@link Long}
     * @return {@link Service}
     */
    @Query(nativeQuery = true,
        value = "select * from service s "
            + "join courier c on c.id = s.courier_id "
            + "join orders o on c.id = o.courier_id "
            + "where o.id = :orderId and c.id = :courierId")
    Service findServiceByOrderIdAndCourierId(@Param("orderId") Long orderId, @Param("courierId") Long courierId);
}
