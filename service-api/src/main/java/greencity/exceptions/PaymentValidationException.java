package greencity.exceptions;

/**
 * Exception that is thrown if order response from fondy isn't valid.
 */
public class PaymentValidationException extends RuntimeException {
    /**
     * Default constructor.
     */
    public PaymentValidationException() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public PaymentValidationException(String message) {
        super(message);
    }
}
