package greencity.ubstelegrambot;

import greencity.constant.ErrorMessage;
import greencity.entity.order.Order;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.MessageWasNotSend;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
@RequiredArgsConstructor
public class TelegramService {
    private final UBSTelegramBot ubsTelegramBot;

    /**
     * The method send a message to users when they non-payment of the order within
     * three days.
     */
    public void sendMessageWhenOrderNonPayment(UBSuser ubsUser) {
        SendMessage sendMessage = new SendMessage(
            ubsUser.getUser().getTelegramBot().getChatId().toString(), "Вас є неоплачені замовлення");
        sendMessageToUser(sendMessage);
    }

    /**
     * The method sends a message to users when a garbage truck arrives to pick up
     * their garbage.
     */
    public void sendMessageWhenGarbageTruckArrives(Order order) {
        SendMessage sendMessage = new SendMessage(
            order.getUser().getTelegramBot().getChatId().toString(),
            "Машина по забору сміття прибуде до вас з "
                + order.getDeliverFrom() + " до " + order.getDeliverTo());
        sendMessageToUser(sendMessage);
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
