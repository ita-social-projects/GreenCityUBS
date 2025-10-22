package greencity.service.ubs.payment;

/**
 * Service for handling payment status updates and synchronizing them with the
 * corresponding order and payment objects.
 */
public interface PaymentStatusHandlerService {
    /**
     * Handles the case when a payment status is approved. Updates the payment and
     * order status to {PAID}, saves changes to repositories, removes payment links
     * from notifications, and logs/records events.
     *
     * @param paymentId          the payment to update
     * @param orderId                 the order to update
     * @param decodedOrderReference decoded reference string, used to extract
     *                              paymentId
     * @param status                the transaction status returned from the payment
     *                              system
     */
    void checkOrderStatusApproved(Long paymentId, Long orderId, String decodedOrderReference, String status);

    /**
     * Handles the case when a payment fails. Updates the payment and order status
     * to {UNPAID} and persists changes.
     *
     * @param paymentId the payment to update
     * @param orderId        the order to update
     * @param status       the transaction status returned from the payment system
     */
    void checkResponseStatusFailure(Long paymentId, Long orderId, String status);
}
