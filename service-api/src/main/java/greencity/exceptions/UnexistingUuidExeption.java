package greencity.exceptions;

public class UnexistingUuidExeption extends RuntimeException {
    /**
     * Default constructor.
     */
    public UnexistingUuidExeption() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public UnexistingUuidExeption(String message) {
        super(message);
    }
}
