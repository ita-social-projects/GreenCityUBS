package greencity.exceptions.employee;

public class EmployeeIsNotAssigned extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public EmployeeIsNotAssigned(String message) {
        super(message);
    }
}
