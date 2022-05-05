package greencity.ubsviberbot;

import greencity.client.UserRemoteClient;
import greencity.client.ViberClient;
import greencity.constant.ErrorMessage;
import greencity.dto.notification.NotificationDto;
import greencity.dto.user.UserVO;
import greencity.dto.viber.dto.SendMessageToUserDto;
import greencity.dto.viber.dto.WebhookDto;
import greencity.dto.viber.enums.EventTypes;
import greencity.dto.viber.enums.MessageType;
import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import greencity.entity.viber.ViberBot;
import greencity.exceptions.MessageWasNotSend;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.UnexistingUuidExeption;
import greencity.exceptions.ViberBotAlreadyConnected;
import greencity.repository.NotificationTemplateRepository;
import greencity.repository.UserRepository;
import greencity.repository.ViberBotRepository;
import greencity.service.NotificationServiceImpl;
import greencity.service.notification.NotificationProvider;
import greencity.service.ubs.ViberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

import static greencity.entity.enums.NotificationReceiverType.OTHER;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViberServiceImpl implements ViberService, NotificationProvider {
    private final ViberClient viberClient;
    private final UserRemoteClient userRemoteClient;
    private final UserRepository userRepository;
    private final ViberBotRepository viberBotRepository;
    private final NotificationTemplateRepository templateRepository;
    @Value("${greencity.bots.viber-bot-url}")
    private String viberBotUrl;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<String> setWebhook() {
        WebhookDto setWebhookDto = WebhookDto.builder()
            .url(viberBotUrl)
            .eventTypes(Set.of(
                EventTypes.delivered, EventTypes.seen, EventTypes.failed, EventTypes.subscribed,
                EventTypes.unsubscribed, EventTypes.conversation_started))
            .build();
        return viberClient.updateWebHook(setWebhookDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<String> removeWebHook() {
        WebhookDto removeWebhookDto = WebhookDto.builder()
            .url("").build();
        return viberClient.updateWebHook(removeWebhookDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<String> getAccountInfo() {
        return viberClient.getAccountInfo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendWelcomeMessageAndPreRegisterViberBotForUser(String receiverId, String uuid) {
        User user = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new UnexistingUuidExeption(ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        if (user.getViberBot() == null) {
            viberBotRepository.save(ViberBot.builder()
                .chatId(receiverId)
                .isNotify(false)
                .user(user)
                .build());
        } else {
            throw new ViberBotAlreadyConnected(ErrorMessage.THE_USER_ALREADY_HAS_CONNECTED_TO_VIBER_BOT);
        }
        SendMessageToUserDto sendMessageToUserDto = SendMessageToUserDto.builder()
            .receiver(receiverId)
            .type(MessageType.text)
            .text("Привіт!\nЦе UbsBot!\n"
                + "Надішли будь який символ для того щоб підписатись на бота і отримувати сповіщення.")
            .build();
        sendMessageToUser(sendMessageToUserDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessageAndRegisterViberBotForUser(String receiverId) {
        ViberBot viberBot = viberBotRepository
            .findViberBotByChatId(receiverId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.THE_CHAT_ID_WAS_NOT_FOUND));
        Boolean check = viberBot.getChatId().equals(receiverId) && !viberBot.getIsNotify();
        if (Boolean.TRUE.equals(check)) {
            viberBot.setIsNotify(true);
            viberBotRepository.save(viberBot);
            SendMessageToUserDto sendMessageToUserDto = SendMessageToUserDto.builder()
                .receiver(receiverId)
                .type(MessageType.text)
                .text("Вітаємо!\nВи підписались на UbsBot")
                .build();
            sendMessageToUser(sendMessageToUserDto);
        } else {
            SendMessageToUserDto sendMessageToUserDto = SendMessageToUserDto.builder()
                .receiver(receiverId)
                .type(MessageType.text)
                .text("Упс!\nВи вже підписані на UbsBot")
                .build();
            sendMessageToUser(sendMessageToUserDto);
        }
    }

    private void sendMessageToUser(SendMessageToUserDto sendMessageToUserDto) {
        try {
            viberClient.sendMessage(sendMessageToUserDto);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error(ErrorMessage.INTERRUPTED_EXCEPTION);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new MessageWasNotSend(ErrorMessage.THE_MESSAGE_WAS_NOT_SEND);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendNotification(UserNotification notification) {
        UserVO userVO =
            userRemoteClient.findNotDeactivatedByEmail(notification.getUser().getRecipientEmail()).orElseThrow();
        NotificationDto notificationDto = NotificationServiceImpl
            .createNotificationDto(notification, userVO.getLanguageVO().getCode(), OTHER, templateRepository);

        if (Objects.nonNull(notification.getUser().getViberBot())
            && Objects.nonNull(notification.getUser().getViberBot().getChatId())) {
            SendMessageToUserDto sendMessageToUserDto = SendMessageToUserDto.builder()
                .receiver(notification.getUser().getViberBot().getChatId())
                .type(MessageType.text)
                .text(notificationDto.getTitle() + "\n\n" + notificationDto.getBody())
                .build();
            log.info("Sending message for user {}, with type {}", notification.getUser().getUuid(),
                notification.getNotificationType());
            sendMessageToUser(sendMessageToUserDto);
        }
    }
}
