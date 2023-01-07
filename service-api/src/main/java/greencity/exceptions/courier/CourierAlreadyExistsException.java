package greencity.exceptions.courier;

public class CourierAlreadyExistsException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public CourierAlreadyExistsException(String message) {
        super(message);
    }
}
