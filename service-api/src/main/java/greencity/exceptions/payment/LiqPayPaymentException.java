package greencity.exceptions.payment;

/**
 * Exception that throw if get some issue with getting status from LiqPay.
 */
public class LiqPayPaymentException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public LiqPayPaymentException(String message) {
        super(message);
    }
}
