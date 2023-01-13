package greencity.exceptions.tariff;

public class TariffAlreadyExists extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public TariffAlreadyExists(String message) {
        super(message);
    }
}
