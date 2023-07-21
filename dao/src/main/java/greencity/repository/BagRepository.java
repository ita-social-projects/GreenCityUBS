package greencity.repository;

import greencity.entity.order.Bag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface BagRepository extends JpaRepository<Bag, Integer> {
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
    @Query(value = "SELECT name, b.capacity, (b.price/100.00) AS price, "
        + "obm.amount, ((b.price * obm.amount) / 100.00) AS summ "
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
     * method, that returns {@link List} of {@link Bag} by id.
     *
     * @param bagId {@link Integer} tariff service id
     * @return {@link Optional} of {@link Bag}
     * @author Julia Seti
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM bag "
            + "WHERE id = :bagId")
    Optional<Bag> findActiveBagById(Integer bagId);

    /**
     * method, that returns {@link List} of active {@link Bag} by tariff id.
     *
     * @param tariffInfoId {@link Long} tariff id
     * @return {@link List} of {@link Bag}
     * @author Julia Seti
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM bag "
            + "WHERE tariffs_info_id = :tariffInfoId")
    List<Bag> findAllActiveBagsByTariffsInfoId(Long tariffInfoId);
}