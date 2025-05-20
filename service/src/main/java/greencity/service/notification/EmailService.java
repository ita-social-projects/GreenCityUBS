package greencity.service.notification;

import greencity.client.UserRemoteClient;
import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.ScheduledEmailMessage;
import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import greencity.enums.NotificationReceiverType;
import greencity.repository.NotificationTemplateRepository;
import java.util.Objects;

import greencity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static greencity.enums.NotificationReceiverType.EMAIL;

@Service
public class EmailService extends AbstractNotificationProvider {
    private final UserRemoteClient userRemoteClient;

    private static final NotificationReceiverType notificationType = EMAIL;

    /**
     * Constructor with super() call.
     */
    @Autowired
    public EmailService(UserRemoteClient userRemoteClient, NotificationTemplateRepository templateRepository, UserRepository userRepository) {
        super(userRemoteClient, templateRepository, notificationType, userRepository);
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
        String userLanguage = userRemoteClient.findUserLanguageByUuid(notification.getUser().getUuid());
        ScheduledEmailMessage emailNotification = ScheduledEmailMessage.builder()
            .username(notification.getUser().getRecipientName())
            .userUuid(notification.getUser().getUuid())
            .subject(notificationDto.getTitle())
            .body(notificationDto.getBody())
            .language(userLanguage)
            .isUbs(true)
            .build();

        userRemoteClient.sendScheduledEmailNotification(emailNotification);
    }
}
