package greencity.ubstelegrambot;

import greencity.constant.ErrorMessage;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.MessageWasNotSend;
import greencity.repository.OrderRepository;
import greencity.repository.UBSuserRepository;
import greencity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UBSBotService {
    private final UBSuserRepository ubSuserRepository;
    private final UBSTelegramBot ubsTelegramBot;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    /**
     * The method send a message to users when they non-payment of the order within
     * three days.
     */
    public void sendMessageWhenOrderNonPayment() {
        List<UBSuser> ubSusers = ubSuserRepository.getAllUBSusersWhoHaveNotPaid(LocalDate.now().minusDays(3));
        for (UBSuser ubsUser : ubSusers) {
            SendMessage sendMessage = new SendMessage(
                ubsUser.getUser().getTelegramBot().getChatId().toString(), "Вас є неоплачені замовлення");
            sendMessageToUser(sendMessage);
        }
    }

    /**
     * The method sends a message to users when a garbage truck arrives to pick up
     * their garbage.
     */
    public void sendMessageWhenGarbageTruckArrives() {
        List<Order> orders = orderRepository.getAllUsersInWhichTheRouteIsDefined();
        for (Order order : orders) {
            SendMessage sendMessage = new SendMessage(
                order.getUser().getTelegramBot().getChatId().toString(),
                "Машина по забору сміття прибуде до вас з "
                    + order.getDeliverFrom() + " до " + order.getDeliverTo());
            sendMessageToUser(sendMessage);
        }
    }

    private void sendMessageToUser(SendMessage sendMessage) {
        try {
            ubsTelegramBot.execute(sendMessage);
            Thread.sleep(2000);
        } catch (Exception e) {
            throw new MessageWasNotSend(ErrorMessage.THE_MESSAGE_WAS_NOT_SEND);
        }
    }
}
