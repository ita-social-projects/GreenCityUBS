package greencity.exceptions;

public class TooManyCertificatesEntered extends RuntimeException {
    /**
     * Default constructor.
     */
    public TooManyCertificatesEntered() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public TooManyCertificatesEntered(String message) {
        super(message);
    }
}
