package greencity.repository;

import greencity.entity.enums.PaymentStatus;
import greencity.entity.notifications.UserNotification;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

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
}
