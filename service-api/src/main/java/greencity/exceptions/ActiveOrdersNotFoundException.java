package greencity.exceptions;

/**
 * Exception shows that not any active order left.
 *
 * @author Oleh Bilonizhka
 */
public class ActiveOrdersNotFoundException extends RuntimeException {
    /**
     * Constructor.
     */
    public ActiveOrdersNotFoundException(String message) {
        super(message);
    }
}
