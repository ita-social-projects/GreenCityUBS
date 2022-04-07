package greencity.repository;

import greencity.entity.user.Location;
import greencity.entity.user.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    /**
     * {@inheritDoc}
     */
    @Query(value = "SELECT * FROM locations as L "
        + "where L.id in ( "
        + "SELECT DISTINCT B.location_id  FROM ORDER_BAG_MAPPING as OBM "
        + "JOIN BAG AS B ON OBM.ORDER_ID = :orderId and OBM.BAG_ID = B.ID ) "
        + "LIMIT 1 ", nativeQuery = true)
    Location findByOrderId(@Param("orderId") Long id);

    /**
     * {@inheritDoc}
     */
    @Query(nativeQuery = true,
        value = "select * FROM locations as l "
            + "join location_translations lt on l.id = lt.location_id "
            + "where lt.location_name = :locationName")
    Optional<Location> findLocationByName(@Param("locationName") String locationName);

    /**
     * Method for get info about region.
     *
     * @return {@link Location}
     * @author Yurii Fedorko
     */
    @Query(nativeQuery = true,
        value = "select * from locations as l " +
                "join regions as r " +
                "on l.region_id = r.id " +
                "where l.location_status = 'ACTIVE'")
    List<Location> findAllActive();
}
