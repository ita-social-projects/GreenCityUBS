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

    /**
     * Method to check if the courier exists by courier id.
     *
     * @param id - courier id.
     * @return return true if courier exists and false if not.
     * @author Nikita Korzh.
     */
    boolean existsCourierById(Long id);
}
