package greencity.exceptions.number;

/**
 * Exception is thrown when eco number format isn`t correct.
 */
public class IncorrectEcoNumberFormatException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public IncorrectEcoNumberFormatException(String message) {
        super(message);
    }
}