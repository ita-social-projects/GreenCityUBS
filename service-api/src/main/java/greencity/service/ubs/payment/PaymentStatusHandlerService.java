package greencity.service.ubs.payment;

import greencity.entity.order.Order;
import greencity.entity.order.Payment;

//TODO add test
/**
 * Service for handling payment status updates and synchronizing them
 * with the corresponding {@link Order} and {@link Payment} entities.
 */
public interface PaymentStatusHandlerService {
    /**
     * Handles the case when a payment status is approved.
     * Updates the payment and order status to {PAID}, saves changes to repositories,
     * removes payment links from notifications, and logs/records events.
     *
     * @param orderPayment           the {@link Payment} entity to update
     * @param order                  the {@link Order} entity to update
     * @param decodedOrderReference  decoded reference string, used to extract paymentId
     * @param status                 the transaction status returned from the payment system
     */
    void checkOrderStatusApproved(Payment orderPayment,
                                  Order order,
                                  String decodedOrderReference,
                                  String status);

    /**
     * Handles the case when a payment fails.
     * Updates the payment and order status to {UNPAID} and persists changes.
     *
     * @param orderPayment the {@link Payment} entity to update
     * @param order        the {@link Order} entity to update
     * @param status       the transaction status returned from the payment system
     */
    void checkResponseStatusFailure(Payment orderPayment,
                                    Order order,
                                    String status);
}
