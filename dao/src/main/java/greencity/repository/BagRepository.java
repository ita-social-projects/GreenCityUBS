package greencity.repository;

import greencity.entity.order.Bag;
import greencity.entity.order.TariffsInfo;
import greencity.enums.MinAmountOfBag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface BagRepository extends JpaRepository<Bag, Integer> {
    /**
     * method, that returns {@link List}of{@link Bag} that have bags by order id.
     *
     * @param id order id
     * @return {@link List}of{@link Bag} by it's language and orderId.
     * @author Mahdziak Orest
     */
    @Query(value = "SELECT * FROM ORDER_BAG_MAPPING as OBM "
        + "JOIN BAG AS B ON OBM.ORDER_ID = :orderId and OBM.BAG_ID = B.ID "
        + "ORDER BY B.ID", nativeQuery = true)
    List<Bag> findBagsByOrderId(@Param("orderId") Long id);

    /**
     * This is method which find capacity by id.
     * 
     * @param bagId {@link Integer}.
     * @return {@link Integer}.
     * @author Yuriy Bahlay.
     */
    @Query(value = "SELECT b.capacity FROM Bag AS b WHERE b.id =:bagId")
    Integer findCapacityById(@Param("bagId") Integer bagId);

    /**
     * method, that returns {@link Bag}'s additional info.
     *
     * @param orderId        order id {@link Long}
     * @param recipientEmail {@link String}
     * @author Nazar Struk
     */
    @Query(value = "SELECT  distinct u.recipient_name , u.recipient_phone , "
        + "u.recipient_email, a.city , a.street , a.house_number , a.district , a.address_comment, "
        + "(SELECT string_agg(payment_id,',') AS pay_id "
        + "FROM payment WHERE recipient_email = :recipientEmail) "
        + "FROM payment "
        + "JOIN orders o on o.id = payment.order_id "
        + "JOIN users u on u.id = o.users_id "
        + "JOIN ubs_user uu on uu.id = o.ubs_user_id "
        + "JOIN order_address a on uu.id = a.id "
        + "WHERE ORDER_ID = :orderId", nativeQuery = true)
    List<Map<String, Object>> getAdditionalBagInfo(Long orderId, String recipientEmail);

    /**
     * method, that returns {@link Bag}'s info.
     * 
     * @param orderId order id {@link Long}
     * @author Nazar Struk
     * @author José Castellanos
     */
    @Query(value = "SELECT name, b.capacity, b.price, obm.amount, (b.price * obm.amount) AS summ "
        + "FROM bag b "
        + "JOIN order_bag_mapping obm on b.id = obm.bag_id "
        + "WHERE obm.ORDER_ID = :orderId", nativeQuery = true)
    List<Map<String, Object>> getBagInfo(Long orderId);

    /**
     * method, that returns {@link List}of{@link Bag}.
     *
     * @param orderId order id
     * @return {@link List}of{@link Bag} by orderId.
     * @author José Castellanos
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM order_bag_mapping AS obm JOIN bag AS b ON obm.bag_id = b.id "
            + "WHERE obm.order_id = :orderId")
    List<Bag> findAllByOrder(@Param("orderId") Long orderId);

    /**
     * method, that returns {@link List} of {@link Bag} by tariff id.
     *
     * @param tariffInfoId tariff id {@link Long}
     * @return {@link List} of {@link Bag} by tariffInfoId.
     * @author Safarov Renat
     */
    @Query(value = "SELECT b FROM Bag as b where b.tariffsInfo.id =:tariffInfoId")
    List<Bag> findBagsByTariffInfoId(@Param("tariffInfoId") Long tariffInfoId);

    /**
     * method, that returns {@link List} of {@link Bag}'s that matches by
     * {@link TariffsInfo} and {@link MinAmountOfBag}.
     *
     * @param tariffId       order id {@link TariffsInfo}
     * @param minAmountOfBag order id {@link MinAmountOfBag}
     * @author Oleg Vatuliak
     */
    List<Bag> getBagsByTariffsInfoAndMinAmountOfBags(TariffsInfo tariffId, MinAmountOfBag minAmountOfBag);

    /**
     * method, that returns {@link List} of {@link Bag} by Tariff id.
     *
     * @param tariffId {@link Long} - tariff id.
     * @return {@link List}of{@link Bag}.
     * @author Julia Seti
     */
    List<Bag> getAllByTariffsInfoId(Long tariffId);

    /**
     * method, that returns {@link List} of {@link Bag} by location id.
     *
     * @param locationId {@link Long} location id.
     * @return {@link List} of {@link Bag} by location id.
     * @author Julia Seti
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM bag b "
            + "JOIN tariffs_info ti on ti.id = b.tariffs_info_id "
            + "JOIN tariffs_locations tl on ti.id = tl.tariffs_info_id "
            + "WHERE tl.location_id =:locationId and ti.location_status = 'ACTIVE'")
    List<Bag> findBagsByLocationIdAndLocationStatusIsActive(@Param("locationId") Long locationId);
}