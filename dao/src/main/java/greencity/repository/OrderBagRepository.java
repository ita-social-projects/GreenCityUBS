package greencity.repository;

import greencity.entity.order.OrderBag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OrderBagRepository extends JpaRepository<OrderBag, Long> {
    /**
     * Deletes the OrderBag from the ORDER_BAG_MAPPING table where the bagId and
     * orderId match the provided values.
     *
     * @param bagId   The ID of the bag to be deleted.
     * @param orderId The ID of the order to which the bag is associated.
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM ORDER_BAG_MAPPING WHERE BAG_ID = :bagId AND ORDER_ID = :orderId", nativeQuery = true)
    void deleteOrderBagByBagIdAndOrderId(@Param("bagId") Integer bagId, @Param("orderId") Long orderId);

    /**
     * Retrieves a list of order bags based on the given bag ID.
     *
     * @param id the ID of the order
     * @return a list of order bags matching the bag ID
     */
    @Query(value = "SELECT   * FROM ORDER_BAG_MAPPING as OBM "
        + "where OBM.BAG_ID = :bagId", nativeQuery = true)
    List<OrderBag> findOrderBagsByBagId(@Param("bagId") Integer id);

    /**
     * Retrieves a list of order bags based on the given order ID.
     *
     * @param id the ID of the order
     * @return a list of order bags matching the order ID
     */
    @Query(value = "SELECT   * FROM ORDER_BAG_MAPPING as OBM "
        + "where OBM.ORDER_ID = :orderId", nativeQuery = true)
    List<OrderBag> findOrderBagsByOrderId(@Param("orderId") Long id);

    /**
     * method updates the bag data of OrderBag for all unpaid orders.
     *
     * @param bagId    {@link Integer} bag id
     * @param capacity {@link Integer} bag capacity
     * @param price    {@link Long} bag full price in coins
     * @param name     {@link String} bag name
     * @param nameEng  {@link String} bag english name
     * @author Oksana Spodaryk
     */
    @Transactional
    @Modifying
    @Query(value = "update order_bag_mapping obm "
        + "set capacity = :capacity, price = :price, name = :name, name_eng = :nameEng "
        + "from orders o "
        + "where o.id = obm.order_id and obm.bag_id = :bagId and o.order_payment_status = 'UNPAID'", nativeQuery = true)
    void updateAllByBagIdForUnpaidOrders(Integer bagId, Integer capacity, Long price, String name, String nameEng);

    /**
     * method returns all OrderBags by bag id.
     *
     * @param bagId {@link Integer} bag id
     * @return {@link List} of {@link OrderBag}
     * @author Julia Seti
     */
    List<OrderBag> findAllByBagId(Integer bagId);
}
