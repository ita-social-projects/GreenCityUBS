package greencity.exceptions;

public class FileNotSavedException extends RuntimeException {
    /**
     * Default constructor.
     */
    public FileNotSavedException() {
    }

    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public FileNotSavedException(String message) {
        super(message);
    }
}
