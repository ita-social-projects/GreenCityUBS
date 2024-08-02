package greencity.exceptions.http;

public class AccessDeniedException extends org.springframework.security.access.AccessDeniedException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public AccessDeniedException(String message) {
        super(message);
    }
}
