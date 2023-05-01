package greencity.ubstelegrambot;

import greencity.client.UserRemoteClient;
import greencity.constant.ErrorMessage;
import greencity.dto.notification.NotificationDto;
import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import greencity.enums.NotificationReceiverType;
import greencity.enums.NotificationType;
import greencity.exceptions.bots.MessageWasNotSent;
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
public class TelegramService extends AbstractNotificationProvider {
    private final UBSTelegramBot ubsTelegramBot;

    private static final NotificationReceiverType notificationType = MOBILE;

    /**
     * Constructor with super() call.
     */
    @Autowired
    public TelegramService(UBSTelegramBot ubsTelegramBot,
        UserRemoteClient userRemoteClient,
        NotificationTemplateRepository templateRepository) {
        super(userRemoteClient, templateRepository, notificationType);
        this.ubsTelegramBot = ubsTelegramBot;
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

    private void sendMessageToUser(SendMessage sendMessage) {
        try {
            ubsTelegramBot.execute(sendMessage);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error(ErrorMessage.INTERRUPTED_EXCEPTION);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new MessageWasNotSent(ErrorMessage.THE_MESSAGE_WAS_NOT_SENT);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sendNotification(UserNotification notification, NotificationDto notificationDto) {
        SendMessage sendMessage = new SendMessage(
            notification.getUser().getTelegramBot().getChatId().toString(),
            notificationDto.getTitle() + "\n\n" + notificationDto.getBody());
        log.info("Sending message for user {}, with type {}", notification.getUser().getUuid(),
            notification.getNotificationType());
        sendMessageToUser(sendMessage);
    }
}
