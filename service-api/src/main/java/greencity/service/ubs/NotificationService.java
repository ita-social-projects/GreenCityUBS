package greencity.service.ubs;

import greencity.dto.NotificationDto;
import greencity.entity.notifications.UserNotification;
import greencity.entity.order.Order;
import greencity.repository.NotificationTemplateRepository;

import java.util.List;

public interface NotificationService {
    /**
     * Method that creates notification for unpaid order.
     *
     * @author Ann Sakhno
     */
    void notifyUnpaidOrders();

    /**
     * Method that creates notification for paid order.
     *
     * @author Ann Sakhno
     */
    void notifyPaidOrder(Order order);

    /**
     * Method that creates notification for courier.
     *
     * @author Ann Sakhno
     */
    void notifyCourierItineraryFormed(Order order);

    /**
     * Method that creates notification for half paid package.
     *
     * @author Ann Sakhno
     */
    void notifyHalfPaidPackage(Order order);

    /**
     * Method that creates notification for users bonuses.
     *
     * @author Ann Sakhno
     */
    void notifyBonuses(Order order, Long overpayment);

    /**
     * Method that creates notification for new violations.
     *
     * @author Ann Sakhno
     */
    void notifyAddViolation(Order order);

    /**
     * Method that creates notification for inactive users.
     *
     * @author Ann Sakhno
     */
    void notifyInactiveAccounts();

    /**
     * Method that creates notification for all half paid orders.
     *
     * @author Ann Sakhno
     */
    void notifyAllHalfPaidPackages();

    /**
     * Method that returns list of all notifications for user by UUID.
     *
     * @author Ann Sakhno
     */
    List<NotificationDto> getAllNotificationsForUser(String userUuid, String language);
}
