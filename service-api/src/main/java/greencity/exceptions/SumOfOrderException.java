package greencity.exceptions;

/**
 * Exception that is thrown if sum of order greater than courier limit, or lower
 * than courier limit.
 */
public class SumOfOrderException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public SumOfOrderException(String message) {
        super(message);
    }
}
