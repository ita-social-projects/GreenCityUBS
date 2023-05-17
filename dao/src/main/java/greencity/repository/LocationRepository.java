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
    @Query(nativeQuery = true,
        value = "select * FROM locations as l "
            + "WHERE l.region_id = :regionId AND (l.name_en = :locationNameEn "
            + "OR l.name_uk = :locationNameUk)")
    Optional<Location> findLocationByNameAndRegionId(@Param("locationNameUk") String locationNameUk,
        @Param("locationNameEn") String locationNameEn,
        @Param("regionId") Long regionId);

    /**
     * Method for get all active locations.
     *
     * @return list of {@link Location}
     * @author Yurii Fedorko
     */
    @Query(nativeQuery = true,
        value = "select * from locations as l "
            + "inner join tariffs_locations as m on l.id = m.location_id "
            + "join tariffs_info as t on t.id = m.tariffs_info_id "
            + "join courier as c on c.id = t.courier_id "
            + "where l.location_status = 'ACTIVE' "
            + "AND t.tariff_status = 'ACTIVE' "
            + "AND m.location_status = 'ACTIVE' "
            + "AND c.courier_status = 'ACTIVE'")
    List<Location> findAllActive();

    /**
     * Method for getting all active locations by courier ID.
     *
     * @param courierId - id of courier
     * @return list of {@link Location}
     * @author Anton Bondar
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM locations AS l "
            + "INNER JOIN tariffs_locations AS m ON l.id = m.location_id "
            + "JOIN tariffs_info AS t ON t.id = m.tariffs_info_id "
            + "JOIN courier AS c ON c.id = t.courier_id "
            + "WHERE l.location_status = 'ACTIVE' "
            + "AND t.tariff_status = 'ACTIVE' "
            + "AND m.location_status = 'ACTIVE' "
            + "AND c.courier_status = 'ACTIVE' "
            + "AND c.id = :courierId")
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
            + "AND id IN :locIds")
    List<Location> findAllByIdAndRegionId(@Param("locIds") List<Long> locIds, @Param("regionId") Long regionId);

    /**
     * Method for finding out if the location already exists in the specified
     * region.
     *
     * @param nameUk Ukrainian translation.
     * @param nameEn English translation.
     * @return {@code true} if exists, {@code else} otherwise.
     */
    boolean existsByNameUkAndNameEnAndRegion(String nameUk, String nameEn, Region region);
}
