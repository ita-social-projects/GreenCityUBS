package greencity.exceptions.service;

public class ServiceAlreadyExistsException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public ServiceAlreadyExistsException(String message) {
        super(message);
    }
}
