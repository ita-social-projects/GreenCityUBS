package greencity.repository;

import greencity.entity.order.TariffLocation;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TariffLocationRepository extends JpaRepository<TariffLocation, Long> {
    /**
     * Method for changing tariffsLocation status.
     *
     * @param tariffId    - id of Tariff card
     * @param locationIds - list of location id's where status would be changed
     * @param status      - status to set
     */
    @Modifying
    @Query(nativeQuery = true,
        value = "UPDATE tariffs_locations SET location_status = :status "
            + "WHERE tariffs_info_id = :tariffId AND location_id IN :locationIds ")
    void changeStatusAll(@Param("tariffId") Long tariffId, @Param("locationIds") List<Long> locationIds,
        @Param("status") String status);

    /**
     * Method for finding all TariffLocations where courier already works.
     *
     * @param courierId   - courier id
     * @param locationIds - list of location id's to check
     * @return list of {@link TariffLocation} where courier already works
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM tariffs_locations as l JOIN tariffs_info as t "
            + "ON l.tariffs_info_id = t.id "
            + "WHERE l.location_id IN :locationIds AND t.courier_id = :courierId")
    List<TariffLocation> findAllByCourierIdAndLocationIds(@Param("courierId") Long courierId,
        @Param("locationIds") List<Long> locationIds);

    /**
     * Method for finding TariffLocations by tariffsInfo and location.
     *
     * @param tariffsInfo - tariffsInfo
     * @param location    - location
     * @return boolean {@link Optional} where courier already works
     */
    Optional<TariffLocation> findTariffLocationByTariffsInfoAndLocation(TariffsInfo tariffsInfo, Location location);

    @Query("SELECT tl FROM TariffLocation tl JOIN tl.tariffsInfo ti JOIN tl.location l WHERE ti.id = :tariffId AND l.id = :locationId")
    Optional<TariffLocation> findTariffLocationByTariffsInfoAndLocationId(@Param("tariffId") Long tariffId,
                                                                          @Param("locationId") Long locationId);
}
