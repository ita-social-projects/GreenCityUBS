package greencity.repository;

import greencity.entity.order.OrderBag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
