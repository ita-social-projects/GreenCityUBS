package greencity.repository;

import greencity.entity.order.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    /**
     * Method that return service by id.
     *
     * @param id {@link Long}
     * @return {@link Service}
     * @author Vadym Makitra
     */
    Optional<Service> findServiceById(Long id);

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
