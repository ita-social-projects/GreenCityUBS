package greencity.exceptions;

public class EmployeeAlreadyExist extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public EmployeeAlreadyExist(String message) {
        super(message);
    }
}
