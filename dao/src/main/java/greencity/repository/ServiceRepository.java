package greencity.repository;

import greencity.entity.order.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    /**
     * Method that return Service by order id and courier id.
     * 
     * @param orderId   {@link Long}
     * @param courierId {@link Long}
     * @return {@link Service}
     * @author Vadym Makitra
     */
    @Query(nativeQuery = true,
        value = "select * from service s "
            + "join tariffs_info cl on s.id = cl.courier_id "
            + "join orders o on cl.courier_id = o.tariffs_info_id "
            + "where o.id = :orderId and cl.courier_id = :courierId ")
    Service findServiceByOrderIdAndCourierId(@Param("orderId") Long orderId, @Param("courierId") Long courierId);

    /**
     * Method that return service by id.
     *
     * @param serviceId {@link Long}
     * @return {@link Service}
     * @author Vadym Makitra
     */
    @Query(nativeQuery = true,
        value = "select * from service s where s.id = :serviceId")
    Optional<Service> findServiceById(@Param("serviceId") Long serviceId);

    /**
     * Method that return full price by courier id.
     *
     * @param courierId {@link Long}
     * @return {@link Integer}
     * @author Maksym Kuzbyt
     */
    @Query(nativeQuery = true,
        value = "select full_price from service s "
            + "where s.courier_id = :courierId ")
    Integer findFullPriceByCourierId(@Param("courierId") Long courierId);
}
