package greencity.exceptions.tariff;

public class TariffAlreadyExistsException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public TariffAlreadyExistsException(String message) {
        super(message);
    }
}
