package greencity.service.ubs;

import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.NotificationFullDto;
import greencity.dto.notification.NotificationShortDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.pageble.PageableAdvancedDto;
import greencity.enums.UserCategory;
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
    void notifyPaidOrder(Long orderId);

    /**
     * Method that creates notifications for all orders with status
     * COURIER_ITINERARY_FORMED.
     *
     * @author Denys Ryhal
     */
    void notifyAllCourierItineraryFormed();

    /**
     * Method that creates notification for courier.
     *
     * @author Ann Sakhno
     */
    void notifyCourierItineraryFormed(Long orderId);

    /**
     * Method that creates notification for half paid package.
     *
     * @author Ann Sakhno
     */
    void notifyHalfPaidPackage(Long orderId);

    /**
     * Method that creates notification for users bonuses.
     *
     * @author Ann Sakhno
     */
    void notifyBonuses(Long orderId, Long overpayment);

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
    void notifyChangedViolation(Long violationId, Long orderId);

    /**
     * Method that creates notification when order canceled.
     */
    void notifyBonusesFromCanceledOrder(Long orderId);

    /**
     * Method that creates notification when admin delete user violations.
     *
     * @author Nazar Bokalo
     */
    void notifyDeleteViolation(Long orderId);

    /**
     * Method that creates notifications for all orders with status
     * CANCELED_VIOLATION_THE_RULES_BY_THE_MANAGER.
     *
     * @author Denys Ryhal
     */
    void notifyAllCanceledViolations();

    /**
     * Method that creates notifications for all orders with status
     * CHANGED_IN_RULE_VIOLATION_STATUS.
     *
     * @author Denys Ryhal
     */
    void notifyAllChangedViolations();

    /**
     * Method that creates notifications for all orders with status
     * VIOLATION_THE_RULES.
     *
     * @author Denys Ryhal
     */
    void notifyAllAddedViolations();

    /**
     * Method that creates notifications for all orders with status
     * DONE_OR_CANCELED_UNPAID_ORDER.
     *
     * @author Denys Ryhal
     */
    void notifyAllDoneOrCanceledUnpaidOrders();

    /**
     * Method that creates notifications for all orders with status
     * ORDER_STATUS_CHANGED.
     *
     * @author Denys Ryhal
     */
    void notifyAllChangedOrderStatuses();

    /**
     * Method that creates notifications for all orders with status UNPAID_PACKAGE.
     *
     * @author Denys Ryhal
     */
    void notifyUnpaidPackages();

    /**
     * Method that creates notifications for all orders with status
     * HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF.
     *
     * @author Denys Ryhal
     */
    void notifyAllHalfPaidOrdersWithStatusBroughtByHimself();

    void notifyCustom(Long templateUuid, UserCategory userCategory);

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
     * Method that creates notification for unpaid orders which tariff price was
     * increased.
     *
     * @author Denys Ryhal
     */
    void notifyAllOrdersWithIncreasedTariffPrice(Integer bagId);

    /**
     * Method sends messages by e-mail/notification that order is unpaid.
     *
     * @param orderId     of Order which status was changed
     * @param paymentLink payment link
     * @author Vladyslav Haliara
     */
    void notifyUnpaidOrder(Long orderId, String paymentLink);

    /**
     * Notifies the customer that the order status has been changed to "Brought by
     * himself".
     *
     * @param orderId The order which status was changed.
     * @author Maksym Lenets
     */
    void notifySelfPickupOrder(Long orderId);

    /**
     * Method that returns page with notifications for user by email.
     *
     * @author Ann Sakhno
     */
    PageableAdvancedDto<NotificationShortDto> getAllShortNotificationsForUser(String email,
        String language, Pageable pageable);

    /**
     * Method that returns page with notifications for current user.
     *
     * @author Maksym Kozak
     */
    PageableAdvancedDto<NotificationFullDto> getAllNotificationsForUser(String uuid,
        String language, Pageable pageable);

    /**
     * Method that return notification and set status - is read.
     *
     * @author Ihor Volianskyi
     */
    NotificationDto getNotification(String uuid, Long notificationId, String language);

    /**
     * Retrieves a notification by its ID for a specific user with an option to mark
     * it as read.
     *
     * @param uuid           The UUID of the user requesting the notification
     * @param notificationId The ID of the notification to retrieve.
     * @param language       The language code for localization.
     * @param markAsRead     If true, marks the notification as read; if false, the
     *                       status remains unchanged.
     * @return A {@link NotificationDto} with notification details (title, body, and
     *         images if present).
     * @author Nazar Vavrushchak
     */
    NotificationDto getNotification(String uuid, Long notificationId, String language, boolean markAsRead);

    /**
     * Method that return all quantity of unread notification.
     *
     * @author Igor Boykov
     */
    long getUnreadNotifications(String userUuid);

    /**
     * Notifies that a new order has been created.
     *
     * @param orderId the created order
     *
     * @author Kizerov Dmytro
     */
    void notifyCreatedOrder(Long orderId);

    /**
     * Method to mark specific UserNotification as read.
     *
     * @param notificationId id of userNotification, that should be marked
     *
     * @author Roman Kasarab
     */
    void viewNotification(Long notificationId, String userUuid);

    /**
     * Method to mark specific UserNotification as unread.
     *
     * @param notificationId id of userNotification, that should be marked
     *
     * @author Roman Kasarab
     */
    void unreadNotification(Long notificationId, String userUuid);

    /**
     * Method to delete specific Notification.
     *
     * @param notificationId id of notification, that should be deleted
     * @param userUuid       user
     * @author Roman Kasarab
     */
    void deleteNotification(Long notificationId, String userUuid);

    /**
     * Notify user that order has unpaid status when it was created and not paid.
     * This method is used one time when user create new order.
     *
     * @param orderId               the order to send notification for
     * @param sumToPay              the sum to pay
     * @param paymentSystemResponse payment system response with link to pay order
     *
     * @author Vladyslav Haliara
     */
    void notifyUnpaidOrderPermanently(Long orderId, Long sumToPay, PaymentSystemResponse paymentSystemResponse);

    /**
     * Notify manager that user requested info about green office service from
     * telegram bot.
     *
     * @param userEmail {@link String} an email address that user sent in the tg bot
     *                  chat.
     *
     * @author Chernenko Vitaliy
     */
    void notifyManagerWithNewGreenOfficeRequestFromTelegramBot(String userEmail, String username, String lang);

    /**
     * Notify user that order has been canceled. This method is used when order
     * status changed to CANCELED.
     *
     * @param orderId the order to send notification for
     *
     * @author Rostyslav Zadyraichuk
     */
    void notifyCanceledOrder(Long orderId);
}
