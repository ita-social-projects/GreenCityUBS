package greencity.ubsviberbot;

import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.dto.viber.enums.MessageType;
import greencity.dto.viber.dto.SendMessageToUserDto;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import greencity.entity.viber.ViberBot;
import greencity.exceptions.MessageWasNotSend;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.UnexistingUuidExeption;
import greencity.exceptions.ViberBotAlreadyConnected;
import greencity.repository.UserRepository;
import greencity.repository.ViberBotRepository;
import greencity.service.ubs.ViberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ViberServiceImpl implements ViberService {
    private final RestClient restClient;
    private final UserRepository userRepository;
    private final ViberBotRepository viberBotRepository;

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
    public void sendWelcomeMessageAndPreRegisterViberBotForUser(String receiverId, String context) {
        User user = userRepository.findUserByUuid(context)
            .orElseThrow(() -> new UnexistingUuidExeption(ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        if (user.getViberBot() == null) {
            viberBotRepository.save(ViberBot.builder()
                .chatId(receiverId)
                .isNotify(false)
                .user(user)
                .build());
            ViberBot viberBot = viberBotRepository.findByChatId(receiverId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.THE_CHAT_ID_WAS_NOT_FOUND));
            user.setViberBot(viberBot);
            userRepository.save(user);
        } else {
            throw new ViberBotAlreadyConnected(ErrorMessage.THE_USER_ALREADY_HAS_CONNECTED_TO_VIBER_BOT);
        }
        SendMessageToUserDto sendMessageToUserDto = SendMessageToUserDto.builder()
            .receiver(receiverId)
            .type(MessageType.text)
            .text("Привіт!\nЦе UbsBot!\n"
                + "Надішли будь який символ для того щоб підписатись на бота і отримувати сповіщення.")
            .build();
        restClient.sendWelcomeMessage(sendMessageToUserDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessageAndRegisterViberBotForUser(String receiverId) {
        ViberBot viberBot = viberBotRepository
            .findByChatId(receiverId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.THE_CHAT_ID_WAS_NOT_FOUND));
        if (viberBot.getChatId().equals(receiverId) && viberBot.getIsNotify().equals(false)) {
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

    /**
     * The method send a message to users when they non-payment of the order within
     * three days.
     */
    public void sendMessageWhenOrderNonPayment(UBSuser ubsUser) {
        SendMessageToUserDto sendMessageToUserDto = SendMessageToUserDto.builder()
            .receiver(ubsUser.getUser().getViberBot().getChatId())
            .type(MessageType.text)
            .text("Вас є неоплачені замовлення")
            .build();
        sendMessageToUser(sendMessageToUserDto);
    }

    /**
     * The method sends a message to users when a garbage truck arrives to pick up
     * their garbage.
     */
    public void sendMessageWhenGarbageTruckArrives(Order order) {
        SendMessageToUserDto sendMessageToUserDto = SendMessageToUserDto.builder()
            .receiver(order.getUser().getViberBot().getChatId())
            .type(MessageType.text)
            .text("Машина по забору сміття прибуде до вас з "
                + order.getDeliverFrom() + " до " + order.getDeliverTo())
            .build();
        sendMessageToUser(sendMessageToUserDto);
    }

    private void sendMessageToUser(SendMessageToUserDto sendMessageToUserDto) {
        try {
            restClient.sentMessage(sendMessageToUserDto);
            Thread.sleep(2000);
        } catch (Exception e) {
            throw new MessageWasNotSend(ErrorMessage.THE_MESSAGE_WAS_NOT_SEND);
        }
    }
}
