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
    List<Payment> findAllByOrderId(long orderId);

    /**
     * This method deletes payment by id.
     */
    void deletePaymentById(Long paymentId);

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
