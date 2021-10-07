package greencity.exceptions;

/**
 * Exception that is thrown when service not found by id.
 */
public class ServiceNotFoundException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public ServiceNotFoundException(String message) {
        super(message);
    }
}
