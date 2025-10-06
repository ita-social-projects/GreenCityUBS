package greencity.service.ubs.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import greencity.constant.AppConstant;
import greencity.entity.notifications.NotificationParameter;
import greencity.entity.notifications.UserNotification;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.enums.NotificationType;
import greencity.enums.OrderPaymentStatus;
import greencity.repository.NotificationParameterRepository;
import greencity.repository.OrderRepository;
import greencity.repository.PaymentRepository;
import greencity.repository.UserNotificationRepository;
import greencity.service.ubs.EventService;
import greencity.service.ubs.order.OrderService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentStatusHandlerServiceImplTest {
    @InjectMocks
    private PaymentStatusHandlerServiceImpl paymentStatusHandlerService;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserNotificationRepository userNotificationRepository;
    @Mock
    private NotificationParameterRepository notificationParameterRepository;
    @Mock
    private EventService eventService;
    @Mock
    private OrderService orderService;

    private Payment payment;
    private Order order;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        order = new Order();
        order.setId(1L);
    }

    @Test
    void checkOrderStatusApproved_statusApproved_setsPaid() {
        String decodedOrderReference = "ORD_0_123";
        String status = AppConstant.APPROVED_STATUS;

        paymentStatusHandlerService.checkOrderStatusApproved(payment, order, decodedOrderReference, status);

        assertThat(payment.getPaymentId()).isEqualTo("0");
        assertThat(payment.getPaymentStatus()).isEqualTo(greencity.enums.PaymentStatus.PAID);
        assertThat(order.getOrderPaymentStatus()).isEqualTo(OrderPaymentStatus.PAID);

        verify(paymentRepository).save(payment);
        verify(orderRepository).save(order);
        verify(eventService, times(2)).save(anyString(), anyString(), eq(order));
    }

    @Test
    void checkOrderStatusApproved_statusNotApproved_doesNothing() {
        String decodedOrderReference = "ORD_123_0";
        String status = "PENDING";

        paymentStatusHandlerService.checkOrderStatusApproved(payment, order, decodedOrderReference, status);

        assertThat(payment.getPaymentStatus()).isNull();
        assertThat(order.getOrderPaymentStatus()).isNull();

        verify(paymentRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
        verify(eventService, never()).save(anyString(), anyString(), any());
    }

    @Test
    void checkResponseStatusFailure_statusFailed_setsUnpaid() {
        String status = AppConstant.FAILED_STATUS;

        paymentStatusHandlerService.checkResponseStatusFailure(payment, order, status);

        assertThat(payment.getPaymentStatus()).isEqualTo(greencity.enums.PaymentStatus.UNPAID);
        assertThat(order.getOrderPaymentStatus()).isEqualTo(OrderPaymentStatus.UNPAID);

        verify(paymentRepository).save(payment);
        verify(orderRepository).save(order);
    }

    @Test
    void removePaymentLinkForOrder_deletesExistingParameters() {
        UserNotification notification = new UserNotification();
        NotificationParameter parameter = new NotificationParameter();

        when(userNotificationRepository.findAllUserNotificationByOrderAndNotificationType(
            eq(order), eq(NotificationType.UNPAID_ORDER)))
            .thenReturn(List.of(notification));
        when(notificationParameterRepository.findNotificationParameterByUserNotificationAndKey(
            eq(notification), eq(AppConstant.PAY_BUTTON)))
            .thenReturn(Optional.of(parameter));

        paymentStatusHandlerService.checkOrderStatusApproved(payment, order, "ORD_123_0", AppConstant.APPROVED_STATUS);

        verify(notificationParameterRepository).delete(parameter);
    }
}