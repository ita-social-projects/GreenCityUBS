package greencity.service.ubs.wayforpay;

import greencity.exceptions.payment.InvalidPaymentResponseException;
import greencity.exceptions.payment.PaymentNotFoundException;
import java.util.Map;

/**
 * Service responsible for building the redirect URL for users
 * after WayForPay payment processing.
 * The redirect URL points to the GreenCity client confirmation page
 * and contains query parameters with the orderId and payment status.
 */
public interface WayForPayRedirectService {
    /**
     * Builds a redirect URL for the client after payment processing.
     *
     * <p>The method validates received WayForPay form parameters,
     * extracts order and payment identifiers from the order reference,
     * checks the actual payment status in the database, and generates
     * the final redirect link with orderId and status as query params.</p>
     *
     * @param formParams key-value pairs received from WayForPay callback,
     *                   must contain "orderReference" and "transactionStatus"
     * @return a complete redirect URL pointing to the configured client confirmation page
     * @throws InvalidPaymentResponseException if required parameters are missing or invalid
     * @throws PaymentNotFoundException if no payment record exists for the given order/payment IDs
     */
    String redirectUser(Map<String, String> formParams);
}
