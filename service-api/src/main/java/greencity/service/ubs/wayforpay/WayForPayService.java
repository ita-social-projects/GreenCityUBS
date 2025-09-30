package greencity.service.ubs.wayforpay;

import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.payment.PaymentWayForPayRequestDto;
import greencity.entity.order.Order;

/**
 * Service for integration with the WayForPay payment system.
 * Provides methods to create payment requests and generate payment links for orders.
 */
public interface WayForPayService {
    /**
     * Processes a payment request for the given order and returns the final payment response
     * containing a link for the user to complete the payment.
     *
     * @param order the order to be paid
     * @param sumToPayInCoins total amount to be paid, expressed in coins (cents)
     * @return a {@link PaymentSystemResponse} with orderId and payment link
     */
    PaymentSystemResponse processWayForPay(Order order, long sumToPayInCoins);

    /**
     * Builds a payment request DTO that will be sent to the WayForPay system.
     * The request contains order details, product information, amount, and generated signature.
     *
     * @param orderId the ID of the order
     * @param sumToPayInCoins total amount to be paid, expressed in coins (cents)
     * @return a {@link PaymentWayForPayRequestDto} ready to be sent to WayForPay
     */
    PaymentWayForPayRequestDto formPaymentRequestForWayForPay(Long orderId, long sumToPayInCoins);

    /**
     * Wraps the payment link into a standardized response object for further usage.
     *
     * @param order the order being paid
     * @param link the payment link returned by WayForPay
     * @return a {@link PaymentSystemResponse} containing order ID and payment link
     */
    PaymentSystemResponse getPaymentRequestDto(Order order, String link);

    /**
     * Extracts the invoice link from a WayForPay checkout response.
     *
     * @param wayForPayResponse the raw JSON response received from WayForPay
     * @return the extracted payment link (invoiceUrl)
     */
    String getLinkFromWayForPayCheckoutResponse(String wayForPayResponse);
}
