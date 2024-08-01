package greencity.exceptions.notification;

public class TemplateDeleteException extends RuntimeException {
    /**
     * Constructor with message.
     *
     * @param message message, that explains cause of the exception.
     */
    public TemplateDeleteException(String message) {
        super(message);
    }
}
