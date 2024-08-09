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
import java.util.Set;

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
        value = "SELECT t.* FROM tariffs_info as t "
            + "INNER JOIN tariffs_locations as m "
            + "on t.id = m.tariffs_info_id "
            + "INNER JOIN locations l on m.location_id = l.id "
            + "WHERE t.courier_id = :courierId AND m.location_id = :locationId "
            + "AND l.is_deleted = false")
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
        + " set tariff_status = 'DEACTIVATED'"
        + " from tariffs_info ti"
        + " inner join receiving_stations rs on ti.id = rs.id"
        + " inner join tariffs_locations tl on ti.id = tl.tariffs_info_id"
        + " inner join locations l on tl.id = tl.location_id"
        + " where l.region_id = :regionId and"
        + " rs.id in(:stationsIds)"
        + " and l.is_deleted = false")
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
        + " set tariff_status = 'DEACTIVATED'"
        + " from tariffs_info as ti"
        + " inner join receiving_stations rs on ti.id = rs.id"
        + " inner join tariffs_locations tl on ti.id = tl.tariffs_info_id"
        + " inner join locations l on tl.id = tl.location_id"
        + " where l.region_id = :regionId and"
        + " l.is_deleted = false and"
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
        + " set tariff_status = 'DEACTIVATED'"
        + " from tariffs_info as ti"
        + " inner join receiving_stations rs on ti.id = rs.id"
        + " inner join tariffs_locations tl on ti.id = tl.tariffs_info_id"
        + " inner join locations l on tl.id = tl.location_id"
        + " where l.region_id = :regionId and"
        + " l.is_deleted = false and"
        + " rs.id in(:stationsIds) and"
        + " ti.courier_id = :courierId")
    void deactivateTariffsByCourierAndRegionAndReceivingStations(Long regionId, List<Long> stationsIds, Long courierId);

    /**
     * Method, that returns {@link Set} of {@link TariffsInfo} by bag ids.
     *
     * @param bagIds {@link List} of {@link Integer} list of bag ids.
     * @return {@link Optional} of {@link TariffsInfo}.
     * @author Julia Seti
     */
    @Query("SELECT DISTINCT ti "
        + "FROM TariffsInfo ti "
        + "JOIN ti.tariffLocations tl "
        + "JOIN Bag b ON ti.id = b.tariffsInfo.id "
        + "WHERE tl.location.id = :locationId "
        + "AND b.id IN :bagIds "
        + "AND tl.location.isDeleted = false")
    Optional<TariffsInfo> findTariffsInfoByBagIdAndLocationId(
        @Param("bagIds") List<Integer> bagIds, @Param("locationId") Long locationId);

    /**
     * Method finds tariff by id and receiving employee id.
     *
     * @param tariffId   {@link Long} - tariff id.
     * @param employeeId {@link Long} - employee id.
     * @return {@link Optional} of {@link TariffsInfo}.
     * @author - Julia Seti.
     */

    @Query(nativeQuery = true,
        value = "SELECT ti.id, ti.tariff_status, "
            + "ti.creator_id, "
            + "ti.created_at, "
            + "ti.courier_limits, "
            + "ti.courier_id, "
            + "ti.limit_description, "
            + "ti.min, "
            + "ti.max "
            + "FROM tariff_infos_receiving_employee_mapping te "
            + "LEFT JOIN tariffs_info ti ON ti.id = te.tariffs_info_id "
            + "LEFT JOIN employees e ON te.employee_id = e.id "
            + "WHERE ti.id = :tariffId AND e.id = :employeeId")
    Optional<TariffsInfo> findTariffsInfoByIdForEmployee(Long tariffId, Long employeeId);

    /**
     * Retrieves the tariff ID associated with the specified location ID.
     *
     * @param locationId The ID of the location to retrieve the tariff ID for.
     * @return An Optional containing the tariff ID if found, otherwise an empty
     *         Optional.
     */
    @Query(nativeQuery = true,
        value = "SELECT tariffs_info_id FROM tariffs_locations "
            + "WHERE location_id = :locationId")
    Optional<Long> findTariffIdByLocationId(Long locationId);

    /**
     * Retrieves the tariff ID associated with the specified location ID and courier
     * ID.
     *
     * @param locationId The ID of the location to retrieve the tariff ID for.
     * @param courierId  The ID of the courier to retrieve the tariff ID for.
     * @return An Optional containing the tariff ID if found, otherwise an empty
     *         Optional.
     */
    @Query(nativeQuery = true,
        value = "SELECT tariffs_info_id FROM tariffs_locations AS tl "
            + "INNER JOIN tariffs_info AS ti ON ti.id = tl.tariffs_info_id  "
            + "JOIN courier AS c ON c.id = ti.courier_id "
            + "WHERE tl.location_id = :locationId "
            + "AND tl.location_status = 'ACTIVE' "
            + "AND ti.tariff_status = 'ACTIVE'"
            + "AND c.courier_status = 'ACTIVE' "
            + "AND c.id = :courierId ")
    Optional<Long> findTariffIdByLocationIdAndCourierId(Long locationId, Long courierId);
}
