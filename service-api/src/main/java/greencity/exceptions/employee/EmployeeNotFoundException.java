package greencity.exceptions.employee;

/**
 * Exception thrown when employee doesn't exist.
 */
public class EmployeeNotFoundException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
