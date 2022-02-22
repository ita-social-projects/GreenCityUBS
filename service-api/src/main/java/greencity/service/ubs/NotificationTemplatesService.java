package greencity.service.ubs;

public interface NotificationTemplatesService {
    void updateNotificationTemplateForSITE(String body, String notificationType, long languageId);

    void updateNotificationTemplateForOTHER(String body, String notificationType, long languageId);
}
