package greencity.exceptions;

/**
 * Exception that is thrown when illegal operation has been called.
 */
public class EmployeeIllegalOperationException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public EmployeeIllegalOperationException(String message) {
        super(message);
    }
}
