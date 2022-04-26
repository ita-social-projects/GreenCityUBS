package greencity.service.ubs;

import greencity.dto.NotificationDto;
import greencity.dto.NotificationShortDto;
import greencity.dto.PageableDto;
import greencity.dto.PaymentResponseDto;
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
     * Method that creates notification for paid order from PaymentResponseDto.
     *
     * @author Danylo Hlynskyi
     */
    void notifyPaidOrder(PaymentResponseDto dto);

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
