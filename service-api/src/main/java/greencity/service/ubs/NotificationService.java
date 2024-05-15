package greencity.service.ubs;

import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.NotificationShortDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.entity.order.Order;
import greencity.entity.user.Violation;
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
     * Method that creates notification for paid order from PaymentResponseDto.
     *
     * @author Danylo Hlynskyi
     */
    void notifyPaidOrder(PaymentResponseDto dto);

    void notifyPaidOrder(Long orderId);

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
     * Method that creates notification for users bonuses from cancelled order.
     *
     * @author Danylo Hlynskyi
     */
    void notifyBonusesFromCanceledOrder(Order order);

    /**
     * Method that creates notification for new violations.
     *
     * @author Ann Sakhno
     */
    void notifyAddViolation(Long orderId);

    /**
     * Method that creates notification when edit violations.
     *
     * @author Nazar Bokalo
     */
    void notifyChangedViolation(Violation violation, Long orderId);

    /**
     * Method that creates notification when admin delete user violations.
     *
     * @author Nazar Bokalo
     */
    void notifyDeleteViolation(Long orderId);

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
     * Method sends messages by e-mail/notification that order is unpaid.
     *
     * @param order of {@link Order} Order which status was changed
     * @author Oleh Kulbaba
     */
    void notifyUnpaidOrder(Order order);

    /**
     * Notifies the customer that the order status has been changed to "Brought by
     * himself".
     *
     * @param order The order {@link Order} which status was changed.
     * @author Maksym Lenets
     */
    public void notifySelfPickupOrder(Order order);

    /**
     * Method that returns page with notifications for user by UUID.
     *
     * @author Ann Sakhno
     */
    PageableDto<NotificationShortDto> getAllNotificationsForUser(String userUuid,
        String language, Pageable pageable);

    /**
     * Method that return notification and set status - is read.
     *
     * @author Ihor Volianskyi
     */
    NotificationDto getNotification(String uuid, Long id, String language);

    /**
     * Method that return all quantity of unreaden notification.
     *
     * @author Igor Boykov
     */
    long getUnreadenNotifications(String userUuid);
}
