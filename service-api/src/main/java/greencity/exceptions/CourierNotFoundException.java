package greencity.exceptions;

/**
 * Exception that thrown if courier not found.
 */
public class CourierNotFoundException extends RuntimeException {
    /**
     * Constructor.
     * 
     * @param message - that was throw is exception was thrown.
     */
    public CourierNotFoundException(String message) {
        super(message);
    }
}
