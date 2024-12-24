package greencity.service.ubs;

public interface NotificationTemplatesService {
    /**
     * Method that updates body in notification templates for receiving type SITE.
     *
     * @param body             - new body for template
     * @param notificationType - type of notifications
     * @param languageCode     - language code
     * @author Natalia Kozak
     */
    void updateNotificationTemplateForSITE(String body, String notificationType, String languageCode);

    /**
     * Method that updates body in notification templates for receiving type OTHER.
     *
     * @param body             - new body for template
     * @param notificationType - type of notifications
     * @param languageCode     - language code
     * @author Natalia Kozak
     */
    void updateNotificationTemplateForOTHER(String body, String notificationType, String languageCode);
}
