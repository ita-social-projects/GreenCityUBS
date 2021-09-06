package greencity.exceptions;

/**
 * Exception is thrown when ubs_user doesn't exists.
 */
public class UBSuserNotFoundException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public UBSuserNotFoundException(String message) {
        super(message);
    }
}
