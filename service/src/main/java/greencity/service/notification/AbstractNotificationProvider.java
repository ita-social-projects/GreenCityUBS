package greencity.service.notification;

import greencity.client.UserRemoteClient;
import greencity.constant.ErrorMessage;
import greencity.dto.notification.NotificationDto;
import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import greencity.enums.NotificationReceiverType;
import greencity.exceptions.user.UserNotFoundException;
import greencity.repository.NotificationTemplateRepository;
import greencity.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import jakarta.annotation.PostConstruct;

@RequiredArgsConstructor
@Getter
public abstract class AbstractNotificationProvider {
    private final UserRemoteClient userRemoteClient;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationReceiverType notificationType;
    private final UserRepository userRepository;

    /**
     * Initializes the notification provider.
     */
    @PostConstruct
    protected void init() {
    }

    /**
     * Checks whether the user has this notification provider enabled.
     *
     * @param user {@link User}
     * @return {@code true} if the user has this notification provider enabled,
     *         {@code false} otherwise
     */
    public abstract boolean isEnabled(User user);

    /**
     * Creates a notification and sends it to the user if the user has this provider
     * enabled.
     *
     * @param notification {@link UserNotification} - notification to send.
     */
    public void sendNotification(UserNotification notification, NotificationReceiverType receiverType,
        long monthsOfAccountInactivity) {
        NotificationDto notificationDto = createNotificationDto(notification, receiverType, monthsOfAccountInactivity);
        if (isEnabled(notification.getUser())) {
            sendNotification(notification, notificationDto);
        }
    }

    /**
     * Sends notification to user.
     *
     * @param notification {@link UserNotification} - notification for user.
     * @param dto          {@link NotificationDto} - title and body of notification.
     */
    protected abstract void sendNotification(UserNotification notification, NotificationDto dto);

    /**
     * Creates a {@link NotificationDto} for provider to send a notification.
     *
     * @param notification {@link UserNotification} notification to create DTO from.
     * @return {@link NotificationDto} notification ready for sending.
     */
    protected NotificationDto createNotificationDto(
        UserNotification notification,
        NotificationReceiverType receiverType,
        long monthsOfAccountInactivity) {
        String uuid = userRepository.findUuidByRecipientEmail(notification.getUser().getRecipientEmail())
                .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        String languageCode = userRemoteClient.findUserLanguageByUuid(uuid);
        return NotificationServiceImpl
            .createNotificationDto(notification, languageCode, receiverType, templateRepository,
                monthsOfAccountInactivity);
    }
}
