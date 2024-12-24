package greencity.exceptions.courier;

public class CourierAlreadyExists extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public CourierAlreadyExists(String message) {
        super(message);
    }
}
