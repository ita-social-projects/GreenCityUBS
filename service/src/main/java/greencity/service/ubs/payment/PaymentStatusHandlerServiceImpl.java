package greencity.service.ubs.payment;

import greencity.constant.AppConstant;
import greencity.constant.OrderHistory;
import greencity.entity.notifications.UserNotification;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.enums.NotificationType;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.PaymentStatus;
import greencity.repository.NotificationParameterRepository;
import greencity.repository.OrderRepository;
import greencity.repository.PaymentRepository;
import greencity.repository.UserNotificationRepository;
import greencity.service.ubs.EventService;
import greencity.service.ubs.order.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentStatusHandlerServiceImpl implements PaymentStatusHandlerService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final NotificationParameterRepository notificationParameterRepository;
    private final EventService eventService;
    private final OrderService orderService;

    @Override
    public void checkOrderStatusApproved(Payment orderPayment, Order order,
        String decodedOrderReference, String status) {
        if (status.equals(AppConstant.APPROVED_STATUS)) {
            orderPayment.setPaymentId(decodedOrderReference.split("_")[AppConstant.COUNTER_ORDER_PAYMENT_ID_INDEX]);
            orderPayment.setPaymentStatus(PaymentStatus.PAID);
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            orderPayment.setOrder(order);
            removePaymentLinkAndNotificationForOrder(order);
            paymentRepository.save(orderPayment);
            orderRepository.save(order);
            eventService.save(OrderHistory.ORDER_PAID_UK, OrderHistory.SYSTEM_UK, order);
            eventService.save(OrderHistory.ADD_PAYMENT_SYSTEM_UK + orderPayment.getPaymentId(),
                OrderHistory.SYSTEM_UK, order);
            orderService.cancelPaymentExpiryJob(order.getId());

            log.info("Payment approved: orderId={}, status={}",
                order.getId(), orderPayment.getPaymentStatus());
        } else {
            log.info("Payment not approved: orderId={}, transactionStatus={}",
                order.getId(), status);
        }
    }

    @Override
    public void checkResponseStatusFailure(Payment orderPayment, Order order, String status) {
        if (status.equals(AppConstant.FAILED_STATUS)) {
            orderPayment.setPaymentStatus(PaymentStatus.UNPAID);

            order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);

            paymentRepository.save(orderPayment);
            orderRepository.save(order);
            log.info("Payment failed: orderId={}, transactionStatus={}, paymentStatus={}",
                order.getId(), status, orderPayment.getPaymentStatus());
        }
    }

    private void removePaymentLinkAndNotificationForOrder(Order order) {
        removePaymentLinkForOrder(order);
        removePaymentNotificationForOrder(order);
    }

    private void removePaymentLinkForOrder(Order order) {
        order.setPaymentLink("");
        order.setPaymentLinkExpiry(null);
    }

    private void removePaymentNotificationForOrder(Order order) {
        List<UserNotification> userNotification = userNotificationRepository
            .findAllUserNotificationByOrderAndNotificationType(order, NotificationType.UNPAID_ORDER);

        if (!userNotification.isEmpty()) {
            userNotification.stream()
                .map(notification -> notificationParameterRepository
                    .findNotificationParameterByUserNotificationAndKey(
                        notification, AppConstant.PAY_BUTTON))
                .forEach(parameter -> parameter
                    .ifPresent(notificationParameterRepository::delete));
        }
    }
}
