package greencity.exceptions.bots;

/**
 * Exception show that the message was not send to user.
 *
 * @author Pikhotskyi Vladyslav
 */
public class MessageWasNotSent extends RuntimeException {
    /**
     * Constructor.
     */
    public MessageWasNotSent(String message) {
        super(message);
    }
}
