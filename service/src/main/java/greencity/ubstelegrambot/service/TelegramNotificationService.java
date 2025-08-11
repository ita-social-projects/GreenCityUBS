package greencity.ubstelegrambot.service;

import greencity.client.UserRemoteClient;
import greencity.dto.notification.NotificationDto;
import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import greencity.enums.NotificationReceiverType;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.notification.AbstractNotificationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.util.Objects;
import static greencity.enums.NotificationReceiverType.MOBILE;

@Service
@Slf4j
public class TelegramNotificationService extends AbstractNotificationProvider {
    private final TelegramExecutor telegramExecutor;
    private static final NotificationReceiverType notificationType = MOBILE;

    /**
     * Constructor with super() call.
     */
    @Autowired
    public TelegramNotificationService(UserRemoteClient userRemoteClient,
        NotificationTemplateRepository templateRepository,
        TelegramExecutor telegramExecutor) {
        super(userRemoteClient, templateRepository, notificationType);
        this.telegramExecutor = telegramExecutor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled(User user) {
        if (Objects.isNull(user)) {
            return false;
        }
        return Objects.nonNull(user.getTelegramBot())
            && Objects.nonNull(user.getTelegramBot().getChatId())
            && Objects.equals(user.getTelegramBot().getIsNotify(), true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sendNotification(UserNotification notification, NotificationDto notificationDto) {
        SendMessage sendMessage = new SendMessage(
            notification.getUser().getTelegramBot().getChatId(),
            notificationDto.getTitle() + "\n\n" + notificationDto.getBody());
        log.info("Sending message for user {}, with type {}", notification.getUser().getUuid(),
            notification.getNotificationType());
        telegramExecutor.executeCommand(sendMessage);
    }
}
