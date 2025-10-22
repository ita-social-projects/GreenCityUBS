package greencity.service.ubs.payment;

import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.enums.PaymentSystem;

/**
 * Defines a common strategy interface for processing payments using different
 * payment systems. Each implementation represents a specific payment provider
 * (e.g. WayForPay, MonoBank, etc.).
 */
public interface PaymentStrategy {
    /**
     * Returns the payment system type that this strategy supports.
     *
     * @return the {@link PaymentSystem} used by this strategy
     */
    PaymentSystem getPaymentSystem();

    /**
     * Processes a payment for the given order using the implemented payment system.
     *
     * @param dto             order data received from the client
     * @param orderId           the order to be paid
     * @param sumToPayInCoins the total amount to be paid, in coins
     * @return a {@link PaymentSystemResponse} containing payment result or redirect
     *         data
     */
    PaymentSystemResponse processPayment(OrderResponseDto dto, Long orderId, long sumToPayInCoins);
}
