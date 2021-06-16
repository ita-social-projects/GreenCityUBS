package greencity.exceptions;

/**
 * Exception that is thrown when some field of employee is invalid.
 */
public class EmployeeValidationException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public EmployeeValidationException(String message) {
        super(message);
    }
}
