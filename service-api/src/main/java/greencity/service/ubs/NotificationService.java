package greencity.service.ubs;

import greencity.dto.NotificationDto;
import greencity.dto.PageableDto;
import greencity.entity.order.Order;
import org.springframework.data.domain.Pageable;

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
    PageableDto<NotificationDto> getAllNotificationsForUser(String userUuid, String language, Pageable pageable);

    /**
     * Method that changes the status of the notification - reviewed.
     *
     * @author Ihor Volianskyi
     */
    void reviewNotification(Long id);
}
