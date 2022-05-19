package greencity.exceptions.bots;

/**
 * Exception show that the user already has connected to Viber bot.
 *
 * @author Pikhotskyi Vladyslav
 */
public class ViberBotAlreadyConnected extends RuntimeException {
    /**
     * Constructor.
     */
    public ViberBotAlreadyConnected(String message) {
        super(message);
    }
}
