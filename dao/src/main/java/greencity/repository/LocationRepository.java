package greencity.repository;

import greencity.entity.user.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    /**
     * {@inheritDoc}
     */
    @Query(value = "SELECT * FROM locations as L "
        + "where L.id in ("
        + "SELECT DISTINCT B.location_id  FROM ORDER_BAG_MAPPING as OBM "
        + "JOIN BAG AS B ON OBM.ORDER_ID = :orderId and OBM.BAG_ID = B.ID )"
        + "LIMIT 1", nativeQuery = true)
    Location findByOrderId(@Param("orderId") Long id);
}
