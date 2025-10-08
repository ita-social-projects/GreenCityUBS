package greencity.service.ubs.wayforpay;

import greencity.dto.payment.PaymentResponseWayForPay;
import java.util.Map;

public interface WayForPayResultService {
    /**
     * Processes the form parameters received from WayForPay and converts them into
     * a PaymentResponseWayForPay object. This method performs the following: -
     * Checks that the form parameters are not empty. - Parses the URL-encoded JSON
     * from the first map key into a PaymentResponseDto. - Validates the payment
     * signature using generateResponseSignature. - Calls validatePayment to update
     * the payment status.
     *
     * @param formParams the form parameters received from the WayForPay callback
     *                   (WayForPay send all json in key for some reason and value
     *                   is empty)
     * @return a PaymentResponseWayForPay representing the processed payment;
     *         returns an error response if parameters are empty, invalid, or the
     *         signature check fails
     */
    PaymentResponseWayForPay convertMapIntoPaymentResponseDto(Map<String, String> formParams);
}
