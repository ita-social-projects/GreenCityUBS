package greencity.exceptions;

/**
 * Exception thrown when remote server did not respond.
 */
public class RemoteServerUnavailableException extends RuntimeException {
    /**
     * Constructor.
     *
     * @param message {@link String} - exception message.
     */
    public RemoteServerUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
