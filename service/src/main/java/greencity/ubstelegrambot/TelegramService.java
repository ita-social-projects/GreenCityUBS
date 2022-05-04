package greencity.ubstelegrambot;

import greencity.client.UserRemoteClient;
import greencity.constant.ErrorMessage;
import greencity.dto.NotificationDto;
import greencity.dto.UserVO;
import greencity.entity.notifications.UserNotification;
import greencity.exceptions.MessageWasNotSend;
import greencity.exceptions.UserNotFoundException;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.NotificationServiceImpl;
import greencity.service.notification.NotificationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Objects;

import static greencity.entity.enums.NotificationReceiverType.OTHER;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramService implements NotificationProvider {
    private final UBSTelegramBot ubsTelegramBot;
    private final UserRemoteClient userRemoteClient;
    private final NotificationTemplateRepository templateRepository;

    private void sendMessageToUser(SendMessage sendMessage) {
        try {
            ubsTelegramBot.execute(sendMessage);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error(ErrorMessage.INTERRUPTED_EXCEPTION);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new MessageWasNotSend(ErrorMessage.THE_MESSAGE_WAS_NOT_SEND);
        }
    }

    /**
     * The method sends notifications to users.
     */
    @Override
    public void sendNotification(UserNotification notification) {
        UserVO userVO = userRemoteClient.findNotDeactivatedByEmail(notification.getUser().getRecipientEmail())
            .orElseThrow(() -> new UserNotFoundException("User with this email does not exits"));
        NotificationDto notificationDto = NotificationServiceImpl
            .createNotificationDto(notification, userVO.getLanguageVO().getCode(), OTHER, templateRepository);

        if (Objects.nonNull(notification.getUser().getTelegramBot())
            && Objects.nonNull(notification.getUser().getTelegramBot().getChatId())) {
            SendMessage sendMessage = new SendMessage(
                notification.getUser().getTelegramBot().getChatId().toString(),
                notificationDto.getTitle() + "\n\n" + notificationDto.getBody());
            log.info("Sending message for user {}, with type {}", notification.getUser().getUuid(),
                notification.getNotificationType());
            sendMessageToUser(sendMessage);
        }
    }
}
