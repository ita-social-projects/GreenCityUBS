package greencity.exceptions.image;

public class FileIsNullException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public FileIsNullException(String message) {super(message);}
}
