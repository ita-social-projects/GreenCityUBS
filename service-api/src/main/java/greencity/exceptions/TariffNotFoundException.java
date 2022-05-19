package greencity.exceptions;

/**
 * Exception that thrown if tariff not found.
 */
public class TariffNotFoundException extends RuntimeException {
    /**
     * Constructor.
     *
     * @param message - that was throw is exception was thrown.
     */
    public TariffNotFoundException(String message) {
        super(message);
    }
}
