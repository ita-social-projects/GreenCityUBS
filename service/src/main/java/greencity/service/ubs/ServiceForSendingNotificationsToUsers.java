package greencity.service.ubs;

import greencity.entity.order.Order;
import greencity.entity.user.ubs.UBSuser;
import greencity.repository.OrderRepository;
import greencity.repository.UBSuserRepository;
import greencity.ubstelegrambot.TelegramService;
import greencity.ubsviberbot.ViberServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * The class allows sending notifications to users on Viber and Telegram using
 * the appropriate bots.
 */
@Service
@RequiredArgsConstructor
public class ServiceForSendingNotificationsToUsers {
    private final UBSuserRepository ubSuserRepository;
    private final OrderRepository orderRepository;
    private final TelegramService telegramService;
    private final ViberServiceImpl viberService;

    /**
     * The method sends a message to users on Viber and Telegram when a garbage
     * truck arrives to pick up their garbage.
     */
    public void sendMessageWhenGarbageTruckArrives() {
        List<Order> orders = orderRepository.getAllUsersInWhichTheRouteIsDefined();
        for (Order order : orders) {
            telegramService.sendMessageWhenGarbageTruckArrives(order);
            viberService.sendMessageWhenGarbageTruckArrives(order);
        }
    }

    /**
     * The method send a message to users on Viber and Telegram when they
     * non-payment of the order within three days.
     */
    public void sendMessageWhenOrderNonPayment() {
        List<UBSuser> ubSusers = ubSuserRepository.getAllUBSusersWhoHaveNotPaid(LocalDate.now().minusDays(3));
        for (UBSuser ubSuser : ubSusers) {
            telegramService.sendMessageWhenOrderNonPayment(ubSuser);
            viberService.sendMessageWhenOrderNonPayment(ubSuser);
        }
    }
}
