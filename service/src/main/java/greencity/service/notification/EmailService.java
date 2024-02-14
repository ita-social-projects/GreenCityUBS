package greencity.service.notification;

import greencity.client.UserRemoteClient;
import greencity.dto.notification.NotificationDto;
import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import greencity.enums.NotificationReceiverType;
import greencity.repository.NotificationTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Objects;
import static greencity.enums.NotificationReceiverType.EMAIL;

@Service
public class EmailService extends AbstractNotificationProvider {
    private final UserRemoteClient userRemoteClient;

    private static final NotificationReceiverType notificationType = EMAIL;

    /**
     * Constructor with super() call.
     */
    @Autowired
    public EmailService(UserRemoteClient userRemoteClient, NotificationTemplateRepository templateRepository) {
        super(userRemoteClient, templateRepository, notificationType);
        this.userRemoteClient = userRemoteClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled(User user) {
        if (Objects.isNull(user)) {
            return false;
        }
        return Objects.nonNull(user.getRecipientEmail());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sendNotification(UserNotification notification, NotificationDto notificationDto) {
        userRemoteClient.sendEmailNotification(notificationDto, notification.getUser().getRecipientEmail());
    }
}
