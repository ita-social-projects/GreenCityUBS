package greencity.repository;

import greencity.entity.order.TariffsInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
     * Method for getting set of tariffs.
     *
     * @param tariffId - list of tariffIds.
     * @return - set of tariffs.
     * @author - Nikita Korzh.
     */
    @Query("SELECT ti FROM TariffsInfo ti where ti.id in(:tariffId)")
    Set<TariffsInfo> findTariffsInfoById(@Param("tariffId") List<Long> tariffId);
}
