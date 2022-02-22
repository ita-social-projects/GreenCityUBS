package greencity.service;

import greencity.repository.NotificationTemplateRepository;
import greencity.service.ubs.NotificationTemplatesService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class NotificationTemplatesServiceImpl implements NotificationTemplatesService {
    private final NotificationTemplateRepository notificationTemplateRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNotificationTemplateForSITE(String body, String notificationType, long languageId) {
        notificationTemplateRepository.updateNotificationTemplateForSITE(body, notificationType, languageId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNotificationTemplateForOTHER(String body, String notificationType, long languageId) {
        notificationTemplateRepository.updateNotificationTemplateForOTHER(body, notificationType, languageId);
    }
}
