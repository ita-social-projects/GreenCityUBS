package greencity.repository;

import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends CrudRepository<Payment, Long> {
    /**
     * The method returns undelivered orders to group them.
     *
     * @return list of {@link Order}.
     */
    @Query("select p from Payment p "
        + "join Order o on p.order.id = o.id "
        + "where p.order.id = :orderId")
    List<Payment> paymentInfo(long orderId);

    /**
     * This method deletes payment by id.
     */
    void deletePaymentById(Long paymentId);

    /**
     * This method finds all payments by order.
     *
     * @param order {@link Order}
     * @return {@link List} of {@link Payment}
     */
    List<Payment> findAllByOrder(Order order);

    /**
     * This method find amount by orderId.
     *
     * @param orderId {@link Long}
     * @return {@link Payment}
     * @author Roman Sulymka
     */
    @Query(value = "select distinct amount from payment"
        + " where order_id = :orderId", nativeQuery = true)
    Long findAmountByOrderId(@Param(value = "orderId") Long orderId);

    /**
     * Method return total paid sum made by order.
     *
     * @param orderId - id of order
     * @return {@link Long} total paid sum for this order
     */
    @Query(nativeQuery = true,
        value = "SELECT sum(amount) FROM payment WHERE order_id = :orderId AND payment_status ='PAID'")
    Long selectSumPaid(@Param(value = "orderId") Long orderId);
}
