package greencity.exceptions.payment;

/**
 * Exception that is thrown if get some issue with getting link with Fondy or
 * liqpay payment.
 */

public class PaymentLinkException extends Exception {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public PaymentLinkException(String message) {
        super(message);
    }
}