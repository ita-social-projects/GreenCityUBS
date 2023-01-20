package greencity.exceptions.image;

public class FileIsNullException extends RuntimeException {
    /**
     * Default constructor.
     */
    public FileIsNullException() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public FileIsNullException(String message) {
        super(message);
    }
}
