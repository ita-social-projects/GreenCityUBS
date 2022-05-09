package greencity.service.notification;

import greencity.client.UserRemoteClient;
import greencity.dto.notification.NotificationDto;
import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import greencity.repository.NotificationTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class EmailService extends AbstractNotificationProvider {
    private final UserRemoteClient userRemoteClient;

    /**
     * Constructor with super() call.
     */
    @Autowired
    public EmailService(UserRemoteClient userRemoteClient, NotificationTemplateRepository templateRepository) {
        super(userRemoteClient, templateRepository);
        this.userRemoteClient = userRemoteClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled(User user) {
        return Objects.nonNull(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sendNotification(UserNotification notification, NotificationDto notificationDto) {
        userRemoteClient.sendEmailNotification(notificationDto, notification.getUser().getRecipientEmail());
    }
}
