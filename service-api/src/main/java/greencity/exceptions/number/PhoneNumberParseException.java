package greencity.exceptions.number;

/**
 * Exception is thrown when the parsing of a phone number has failed.
 */
public class PhoneNumberParseException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public PhoneNumberParseException(String message) {
        super(message);
    }
}
