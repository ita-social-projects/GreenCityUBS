package greencity.repository;

import greencity.entity.user.Location;
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
    @Query(nativeQuery = true,
        value = "select l.* FROM locations as l "
            + "WHERE l.region_id = :regionId AND (l.name_en = :locationNameEn "
            + "OR l.name_uk = :locationNameUk) "
            + "AND l.is_deleted = false")
    Optional<Location> findLocationByNameAndRegionId(@Param("locationNameUk") String locationNameUk,
        @Param("locationNameEn") String locationNameEn,
        @Param("regionId") Long regionId);

    /**
     * Method for getting all active locations by courier ID.
     *
     * @param courierId - id of courier
     * @return list of {@link Location}
     * @author Anton Bondar
     */
    @Query(nativeQuery = true,
        value = "SELECT l.* FROM locations AS l "
            + "INNER JOIN tariffs_locations AS m ON l.id = m.location_id "
            + "JOIN tariffs_info AS t ON t.id = m.tariffs_info_id "
            + "JOIN courier AS c ON c.id = t.courier_id "
            + "WHERE l.location_status = 'ACTIVE' "
            + "AND t.tariff_status = 'ACTIVE' "
            + "AND m.location_status = 'ACTIVE' "
            + "AND c.courier_status = 'ACTIVE' "
            + "AND c.id = :courierId "
            + "AND l.is_deleted = false")
    List<Location> findAllActiveLocationsByCourierId(@Param("courierId") Long courierId);

    /**
     * Method for getting list of locations from one region.
     *
     * @param locIds   - list of location ID's
     * @param regionId - id of region
     * @return list of {@link Location}
     * @author Yurii Fedorko
     */
    @Query(nativeQuery = true,
        value = "SELECT * from locations "
            + "WHERE region_id = :regionId "
            + "AND id IN :locIds "
            + "AND is_deleted = false")
    List<Location> findAllByIdAndRegionId(@Param("locIds") List<Long> locIds, @Param("regionId") Long regionId);

    /**
     * Method for getting location from region.
     *
     * @param locationId - location id
     * @param regionId   - region id
     * @return {@link Optional} of {@link Location}
     * @author Julia Seti
     */
    @Query(nativeQuery = true,
        value = "SELECT * from locations "
            + "WHERE region_id = :regionId "
            + "AND id = :locationId "
            + "AND is_deleted = false")
    Optional<Location> findLocationByIdAndRegionId(@Param("locationId") Long locationId,
        @Param("regionId") Long regionId);

    /**
     * Method for getting list of locations by region.
     *
     * @param regionId {@link Long} - id of region
     * @return list of {@link Location}
     * @author Julia Seti
     */
    @Query(nativeQuery = true,
        value = "SELECT * from locations "
            + "WHERE region_id = :regionId "
            + "AND is_deleted = false")
    List<Location> findLocationsByRegionId(@Param("regionId") Long regionId);

    /**
     * Method for not deleted location by id.
     *
     * @param locationId {@link Long} - id of location
     * @return optional of {@link Location}
     * @author Denys Ryhal
     */
    Optional<Location> findByIdAndIsDeletedIsFalse(Long locationId);
}
