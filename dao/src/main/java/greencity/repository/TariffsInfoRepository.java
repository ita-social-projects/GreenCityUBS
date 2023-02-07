package greencity.repository;

import greencity.entity.order.TariffsInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TariffsInfoRepository extends JpaRepository<TariffsInfo, Long>, JpaSpecificationExecutor<TariffsInfo> {
    /**
     * Method for getting TariffInfo.
     *
     * @param courierId  - id of courier
     * @param locationId - id of location
     * @return Optional of {@link TariffsInfo}
     * @author Yurii Fedorko
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM tariffs_info as t "
            + "INNER JOIN tariffs_locations as m "
            + "on t.id = m.tariffs_info_id "
            + "WHERE t.courier_id = :courierId AND m.location_id = :locationId")
    Optional<TariffsInfo> findTariffsInfoLimitsByCourierIdAndLocationId(@Param("courierId") Long courierId,
        @Param("locationId") Long locationId);

    /**
     * Method for getting TariffInfo by last order of user.
     *
     * @param orderId - id of order
     * @return {@link TariffsInfo}
     * @author Yurii Fedorko
     */
    TariffsInfo findTariffsInfoByOrdersId(Long orderId);

    /**
     * Method for getting TariffInfo by order's id.
     *
     * @param orderId - id of order
     * @return - Optional of {@link TariffsInfo} if order with such id exists in DB
     */
    Optional<TariffsInfo> findByOrdersId(@Param("orderId") Long orderId);

    /**
     * Method that deactivate tariffs for region id, list of station ids.
     *
     * @param regionId    - region id.
     * @param stationsIds - list of receiving stations ids.
     * @author Lilia Mokhnatska.
     */
    @Modifying
    @Query(nativeQuery = true, value = "update tariffs_info"
        + " set location_status = 'DEACTIVATED'"
        + " from tariffs_info ti"
        + " inner join receiving_stations rs on ti.id = rs.id"
        + " inner join tariffs_locations tl on ti.id = tl.tariffs_info_id"
        + " inner join locations l on tl.id = tl.location_id"
        + " where l.region_id = :regionId and"
        + " rs.id in(:stationsIds)")
    void deactivateTariffsByRegionAndReceivingStations(Long regionId, List<Long> stationsIds);

    /**
     * Method that deactivate tariffs for region id, list of cities ids and courier
     * id.
     *
     * @param regionId  - region id.
     * @param citiesIds - list of cities ids.
     * @param courierId - courier id.
     * @author Lilia Mokhnatska.
     */
    @Modifying
    @Query(nativeQuery = true, value = "update tariffs_info"
        + " set location_status = 'DEACTIVATED'"
        + " from tariffs_info as ti"
        + " inner join receiving_stations rs on ti.id = rs.id"
        + " inner join tariffs_locations tl on ti.id = tl.tariffs_info_id"
        + " inner join locations l on tl.id = tl.location_id"
        + " where l.region_id = :regionId and"
        + " tl.location_id in (:citiesIds) and"
        + " ti.courier_id = :courierId")
    void deactivateTariffsByCourierAndRegionAndCities(Long regionId, List<Long> citiesIds, Long courierId);

    /**
     * Method that deactivate tariffs for region id, list of station ids and courier
     * id.
     *
     * @param regionId    - region id.
     * @param stationsIds - list of receiving stations ids.
     * @param courierId   - courier id.
     * @author Lilia Mokhnatska.
     */
    @Modifying
    @Query(nativeQuery = true, value = "update tariffs_info"
        + " set location_status = 'DEACTIVATED'"
        + " from tariffs_info as ti"
        + " inner join receiving_stations rs on ti.id = rs.id"
        + " inner join tariffs_locations tl on ti.id = tl.tariffs_info_id"
        + " inner join locations l on tl.id = tl.location_id"
        + " where l.region_id = :regionId and"
        + " rs.id in(:stationsIds) and"
        + " ti.courier_id = :courierId")
    void deactivateTariffsByCourierAndRegionAndReceivingStations(Long regionId, List<Long> stationsIds, Long courierId);
}
