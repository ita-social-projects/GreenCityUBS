package greencity.repository;

import greencity.entity.order.TariffsInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
    @Query(nativeQuery = true,
        value = "SELECT * FROM tariffs_info t JOIN orders o ON t.id = o.tariffs_info_id "
            + "where o.id = :order_id")
    TariffsInfo findTariffsInfoByOrder(@Param("order_id") Long orderId);

    /**
     * Method for searching existing tariff by courier id and specified location
     * id's.
     *
     * @param courierId   - id of courier
     * @param locationIds - list of location id's to check
     * @return list of {@link TariffsInfo} where courier works on card contains
     *         specified locations
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM tariffs_info as t INNER JOIN tariffs_locations as m ON t.id = m.tariffs_info_id "
            + "WHERE courier_id = :courierId AND m.location_id IN :locationIds")
    List<TariffsInfo> findAllByCourierAndAndTariffLocations(@Param("courierId") Long courierId,
        @Param("locationIds") List<Long> locationIds);

    /**
     * Method for getting TariffInfo by order's id.
     *
     * @param orderId - id of order
     * @return - Optional of {@link TariffsInfo} if order with such id exists in DB
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM tariffs_info t INNER JOIN orders o ON t.id = o.tariffs_info_id WHERE o.id = :orderId")
    Optional<TariffsInfo> findByOrderId(@Param("orderId") Long orderId);
}
