package greencity.ubsviberbot;

import greencity.client.OutOfRequestRestClient;
import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.dto.NotificationDto;
import greencity.dto.UserVO;
import greencity.dto.viber.dto.SendMessageToUserDto;
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
import greencity.service.ubs.ViberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViberServiceImpl implements ViberService {
    private final RestClient restClient;
    private final OutOfRequestRestClient outOfRequestRestClient;
    private final UserRepository userRepository;
    private final ViberBotRepository viberBotRepository;
    private final NotificationTemplateRepository templateRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<String> setWebhook() {
        return restClient.setWebhook();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<String> removeWebHook() {
        return restClient.removeWebHook();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<String> getAccountInfo() {
        return restClient.getAccountInfo();
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
        if (viberBot.getChatId().equals(receiverId) && !viberBot.getIsNotify()) {
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
            restClient.sendMessage(sendMessageToUserDto);
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
            outOfRequestRestClient.findUserByEmail(notification.getUser().getRecipientEmail()).orElseThrow();
        NotificationDto notificationDto = NotificationServiceImpl
            .createNotificationDto(notification, userVO.getLanguageVO().getCode(), templateRepository);

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
