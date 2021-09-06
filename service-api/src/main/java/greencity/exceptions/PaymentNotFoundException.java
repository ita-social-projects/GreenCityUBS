package greencity.exceptions;

public class PaymentNotFoundException extends RuntimeException {
    /**
     * Default constructor.
     */
    public PaymentNotFoundException() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public PaymentNotFoundException(String message) {
        super(message);
    }
}
