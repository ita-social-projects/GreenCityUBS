package greencity.exceptions.notification;

public class NotificationAlreadyExists extends RuntimeException {
    public NotificationAlreadyExists(String message) {
        super(message);
    }
}
