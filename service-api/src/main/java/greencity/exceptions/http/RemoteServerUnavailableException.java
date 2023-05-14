package greencity.exceptions.http;

/**
 * Exception thrown when remote server did not respond.
 */
public class RemoteServerUnavailableException extends RuntimeException {
    /**
     * Constructor.
     *
     * @param message {@link String} - exception message.
     * @param cause   {@link Throwable} - cause of exception.
     */
    public RemoteServerUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     *
     * @param message {@link String} - exception message.
     */
    public RemoteServerUnavailableException(String message) {
        super(message);
    }
}
