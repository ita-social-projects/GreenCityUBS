package greencity.repository;

import greencity.entity.order.TariffsInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TariffsInfoRepository extends JpaRepository<TariffsInfo, Long> {
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
            + "INNER JOIN tariffs_info_locations_mapping as m "
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

    @Query(nativeQuery = true, value = "SELECT * FROM tariffs_info as t INNER JOIN tariffs_locations as m ON t.id = m.tariffs_info_id " +
            "WHERE courier_id = :courierId AND m.location_id IN :locationIds")
    List<TariffsInfo> findAllByCourierAndAndTariffLocations(@Param("courierId") Long courierId, @Param("locationIds") List<Long> locationIds);
}
