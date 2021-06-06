package greencity.exceptions;

/**
 * Exception show that we could not find the desired result.
 *
 * @author Pikhotskyi Vladyslav
 */
public class NotFoundException extends RuntimeException {
    /**
     * Constructor for NotFoundException.
     *
     * @param message - giving message.
     */
    public NotFoundException(String message) {
        super(message);
    }
}
