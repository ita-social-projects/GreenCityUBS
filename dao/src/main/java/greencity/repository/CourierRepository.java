package greencity.repository;

import greencity.entity.order.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    /**
     * Method that return courier by order Id.
     * 
     * @param orderId {@link Long}
     * @return {@link Courier}
     */
    @Query(nativeQuery = true,
        value = "select * from courier c "
            + "join courier_locations cl on c.id = cl.courier_id "
            + "join orders o on cl.id = o.courier_locations_id "
            + "where o.id = :orderId")
    Courier findCourierByOrderId(@Param("orderId") Long orderId);
}
