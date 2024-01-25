package greencity.ubsviberbot;

import greencity.client.UserRemoteClient;
import greencity.client.ViberClient;
import greencity.constant.ErrorMessage;
import greencity.dto.notification.NotificationDto;
import greencity.dto.viber.dto.SendMessageToUserDto;
import greencity.dto.viber.dto.WebhookDto;
import greencity.dto.viber.enums.EventTypes;
import greencity.dto.viber.enums.MessageType;
import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import greencity.entity.viber.ViberBot;
import greencity.enums.NotificationReceiverType;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.bots.MessageWasNotSent;
import greencity.exceptions.bots.ViberBotAlreadyConnected;
import greencity.repository.NotificationTemplateRepository;
import greencity.repository.UserRepository;
import greencity.repository.ViberBotRepository;
import greencity.service.notification.AbstractNotificationProvider;
import greencity.service.ubs.ViberService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.Set;
import static greencity.enums.NotificationReceiverType.MOBILE;

@Service
@Slf4j
public class ViberServiceImpl extends AbstractNotificationProvider implements ViberService {
    public static final String WEBHOOK_FIELD = "webhook";
    private final ViberClient viberClient;
    private final UserRepository userRepository;
    private final ViberBotRepository viberBotRepository;
    @Value("${greencity.bots.viber-bot-url}")
    private String viberBotUrl;

    private static final NotificationReceiverType notificationType = MOBILE;

    /**
     * Constructor with super() call.
     */
    @Autowired
    public ViberServiceImpl(ViberClient viberClient,
        UserRemoteClient userRemoteClient,
        UserRepository userRepository,
        ViberBotRepository viberBotRepository,
        NotificationTemplateRepository templateRepository) {
        super(userRemoteClient, templateRepository, notificationType);
        this.viberClient = viberClient;
        this.userRepository = userRepository;
        this.viberBotRepository = viberBotRepository;
    }

    /**
     * Sets the webhook if the right one is not already set.
     */
    @Override
    protected void init() {
        if (!isRightWebhookSet()) {
            setWebhook();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled(User user) {
        if (Objects.isNull(user) || Objects.isNull(user.getViberBot())) {
            return false;
        }
        return Objects.nonNull(user.getViberBot().getIsNotify()) && user.getViberBot().getIsNotify();
    }

    /**
     * {@inheritDoc}
     */
    private boolean isRightWebhookSet() {
        JSONObject accountInfo = new JSONObject(getAccountInfo().getBody());
        return accountInfo.has(WEBHOOK_FIELD) && viberBotUrl.equals(accountInfo.get(WEBHOOK_FIELD).toString());
    }

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
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        if (!isEnabled(user)) {
            ViberBot viberBot = getViberBot(user);
            viberBot.setChatId(receiverId);
            viberBotRepository.save(viberBot);
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

    private ViberBot getViberBot(User user) {
        return viberBotRepository.findByUser(user)
            .orElse(ViberBot.builder()
                .isNotify(false)
                .user(user)
                .build());
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
            throw new MessageWasNotSent(ErrorMessage.THE_MESSAGE_WAS_NOT_SENT);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sendNotification(UserNotification notification, NotificationDto notificationDto) {
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
