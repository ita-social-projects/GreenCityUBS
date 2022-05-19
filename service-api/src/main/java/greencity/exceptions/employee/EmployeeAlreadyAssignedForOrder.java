package greencity.exceptions.employee;

public class EmployeeAlreadyAssignedForOrder extends RuntimeException {
    /**
     * Default constructor.
     */
    public EmployeeAlreadyAssignedForOrder() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public EmployeeAlreadyAssignedForOrder(String message) {
        super(message);
    }
}
