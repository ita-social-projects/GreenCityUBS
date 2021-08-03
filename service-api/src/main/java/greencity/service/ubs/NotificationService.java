package greencity.service.ubs;

import greencity.dto.NotificationDto;
import greencity.entity.order.Order;

import java.util.List;

public interface NotificationService {
    void notifyUnpaidOrders();

    void notifyPaidOrder(Order order);

    void notifyCourierItineraryFormed(Order order);

    void notifyHalfPaidPackage(Order order);

    void notifyBonuses(Order order, Long overpayment);

    void notifyAddViolation(Order order);

    void notifyInactiveAccounts();

    List<NotificationDto> getAllNotificationsForUser(String userUuid, String language);

    void notifyAllHalfPaidPackages();
}
