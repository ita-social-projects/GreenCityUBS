package greencity.repository;

import greencity.entity.order.OrderBag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OrderBagRepository extends JpaRepository<OrderBag, Long> {
    /**
     * method, that returns {@link List} of {@link OrderBag} by bag id for unpaid
     * orders.
     *
     * @param bagId {@link Integer} bag id
     * @return {@link List} of {@link OrderBag}
     * @author Julia Seti
     */
    @Query(value = "SELECT * FROM order_bag_mapping as obm "
        + "JOIN orders o on o.id = obm.order_id "
        + "WHERE obm.bag_id = :bagId AND o.order_payment_status = 'UNPAID'", nativeQuery = true)
    List<OrderBag> findAllOrderBagsForUnpaidOrdersByBagId(Integer bagId);

    /**
     * method updates the bag data of OrderBag for all unpaid orders.
     *
     * @param bagId {@link Integer} bag id
     * @param capacity {@link Integer} bag capacity
     * @param price {@link Long} bag full price in coins
     * @param name {@link String} bag name
     * @param nameEng {@link String} bag english name
     * @author Julia Seti
     */
    @Transactional
    @Modifying
    @Query(value = "update order_bag_mapping obm "
            + "set capacity = :capacity, price = :price, name = :name, name_eng = :nameEng "
            + "from orders o "
            + "where o.id = obm.order_id and obm.bag_id = :bagId and o.order_payment_status = 'UNPAID'", nativeQuery = true)
    void updateAllByBagIdForUnpaidOrders(Integer bagId, Integer capacity, Long price, String name, String nameEng);
}
