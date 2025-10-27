package greencity.service.ubs.wayforpay;

import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.payment.PaymentCancellationWayForPayRequestDto;
import greencity.dto.payment.PaymentWayForPayRequestDto;
import java.util.Set;

/**
 * Service for integration with the WayForPay payment system. Provides methods
 * to create payment requests and generate payment links for orders.
 */
public interface WayForPayService {
    /**
     * Processes a payment request for the given order and returns the final payment
     * response containing a link for the user to complete the payment.
     *
     * @param orderId         the order id to be paid
     * @param sumToPayInCoins total amount to be paid, expressed in coins (cents)
     * @return a {@link PaymentSystemResponse} with orderId and payment link
     */
    PaymentSystemResponse processWayForPay(OrderResponseDto dto, Long orderId, long sumToPayInCoins);

    /**
     * Builds a payment request DTO that will be sent to the WayForPay system. The
     * request contains order details, product information, amount, and generated
     * signature.
     *
     * @param orderId         the ID of the order
     * @param sumToPayInCoins total amount to be paid, expressed in coins (cents)
     * @return a {@link PaymentWayForPayRequestDto} ready to be sent to WayForPay
     */
    PaymentWayForPayRequestDto formPaymentRequestForWayForPay(Long orderId, long sumToPayInCoins);

    /**
     * Wraps the payment link into a standardized response object for further usage.
     *
     * @param orderId the order id being paid
     * @param link    the payment link returned by WayForPay
     * @return a {@link PaymentSystemResponse} containing order ID and payment link
     */
    PaymentSystemResponse getPaymentRequestDto(Long orderId, String link);

    /**
     * Extracts the invoice link from a WayForPay checkout response.
     *
     * @param wayForPayResponse the raw JSON response received from WayForPay
     * @return the extracted payment link (invoiceUrl)
     */
    String getLinkFromWayForPayCheckoutResponse(String wayForPayResponse);

    /**
     * Forms a cancellation request for WayForPay to remove an existing invoice.
     * <br>
     * - Builds a {@link PaymentCancellationWayForPayRequestDto} with encoded order
     * reference. <br>
     * - Generates and sets a signature for request validation.
     *
     * @param orderId the order id to be cancelled
     * @return a fully formed {@link PaymentCancellationWayForPayRequestDto} ready
     *         to send to WayForPay
     */
    PaymentCancellationWayForPayRequestDto formPaymentCancellationRequestForWayForPay(Long orderId);

    /**
     * Extracts the cancellation result from a WayForPay JSON response. <br>
     * - Parses the JSON and returns the “reason” field from it.
     *
     * @param wayForPayResponse the JSON response returned by WayForPay
     * @return the reason message describing the cancellation result
     */
    String getResultFromWayForPayCancellationResponse(String wayForPayResponse);

    /**
     * Schedules a job that cancels unpaid orders after their payment link expires.
     * <br>
     * - Creates a Quartz job with order details, used points, and certificates.
     * <br>
     * - Saves payment link and its expiry time to the order. <br>
     * - Throws an exception if job scheduling fails.
     *
     * @param orderId          the order id for which the expiry job is scheduled
     * @param pointsUsed       number of points used for payment
     * @param certificateCodes set of applied certificate codes (can be empty)
     * @param expirySeconds    the number of seconds until the payment link expires
     * @param paymentLink      the payment link that will expire
     */
    void schedulePaymentExpiryJob(
        Long orderId, int pointsUsed, Set<String> certificateCodes, Long expirySeconds, String paymentLink);
}
