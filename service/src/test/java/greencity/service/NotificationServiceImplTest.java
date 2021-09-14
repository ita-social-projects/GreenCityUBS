package greencity.service;

import greencity.entity.enums.NotificationType;
import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.notifications.NotificationParameter;
import greencity.entity.notifications.UserNotification;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
    private final static LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(1994, 3, 28, 15, 10);

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @Mock
    private NotificationParameterRepository notificationParameterRepository;

    @Mock
    private ViolationRepository violationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BagRepository bagRepository;

    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;

    @Mock
    private Clock clock;

    @Mock
    private ExecutorService executorService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Clock fixedClock;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        fixedClock = Clock.fixed(LOCAL_DATE_TIME.toInstant(ZoneOffset.ofHours(0)), ZoneId.systemDefault());
        lenient().doReturn(fixedClock.instant()).when(clock).instant();
        lenient().doReturn(fixedClock.getZone()).when(clock).getZone();
    }

    @Test
    void testNotifyUnpaidOrders() {
        User user = User.builder().id(42L).build();
        List<Order> orders = List.of(Order.builder().id(42L).user(user)
            .orderPaymentStatus(OrderPaymentStatus.UNPAID)
            .orderDate(LocalDateTime.now(fixedClock).minusMonths(1).plusDays(1)).build(),
            Order.builder().id(50L).user(user)
                .orderPaymentStatus(OrderPaymentStatus.UNPAID)
                .orderDate(LocalDateTime.now(fixedClock).minusDays(9)).build());

        when(orderRepository.findAllByOrderPaymentStatus(OrderPaymentStatus.UNPAID))
            .thenReturn(orders);

        doReturn(Optional.empty()).when(userNotificationRepository)
            .findLastNotificationByNotificationTypeAndOrderNumber(NotificationType.UNPAID_ORDER.toString(),
                orders.get(0).getId().toString());

        UserNotification secondOrderLastNotification = new UserNotification();
        secondOrderLastNotification.setNotificationType(NotificationType.UNPAID_ORDER);
        secondOrderLastNotification.setNotificationTime(LocalDateTime.now(fixedClock).minusDays(8));

        doReturn(Optional.of(secondOrderLastNotification)).when(userNotificationRepository)
            .findLastNotificationByNotificationTypeAndOrderNumber(NotificationType.UNPAID_ORDER.toString(),
                orders.get(1).getId().toString());

        UserNotification created = new UserNotification();
        created.setNotificationType(NotificationType.UNPAID_ORDER);
        created.setNotificationTime(LocalDateTime.now(fixedClock));
        created.setUser(orders.get(0).getUser());
        created.setId(42L);

        when(userNotificationRepository.save(any())).thenReturn(created);

        NotificationParameter createdNotificationParameter = NotificationParameter.builder().id(42L)
            .userNotification(created).key("orderNumber")
            .value(orders.get(0).getId().toString()).build();

        createdNotificationParameter.setUserNotification(created);

        when(notificationParameterRepository.save(any())).thenReturn(createdNotificationParameter);

        notificationService.notifyUnpaidOrders();

        verify(notificationParameterRepository, times(2)).save(any());
        verify(userNotificationRepository, times(2)).save(any());
    }

    @Test
    void testNotifyCourierItineraryFormed() {
        Order order = Order.builder().id(44L).user(User.builder().id(42L).build())
            .deliverFrom(LocalDateTime.now(fixedClock).plusDays(1))
            .deliverFrom(LocalDateTime.now(fixedClock).minusHours(3))
            .deliverTo(LocalDateTime.now(fixedClock).minusHours(2))
            .orderStatus(OrderStatus.ADJUSTMENT)
            .orderPaymentStatus(OrderPaymentStatus.PAID)
            .orderDate(LocalDateTime.now(fixedClock))
            .build();

        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.COURIER_ITINERARY_FORMED);
        userNotification.setUser(order.getUser());
        when(userNotificationRepository.save(any())).thenReturn(userNotification);

        List<NotificationParameter> parameters = new LinkedList<>();
        parameters.add(NotificationParameter.builder().key("date")
            .value(order.getDeliverFrom().format(DateTimeFormatter.ofPattern("dd-MM"))).build());
        parameters.add(NotificationParameter.builder().key("startTime")
            .value(order.getDeliverFrom().format(DateTimeFormatter.ofPattern("hh:mm"))).build());
        parameters.add(NotificationParameter.builder().key("endTime")
            .value(order.getDeliverTo().format(DateTimeFormatter.ofPattern("hh:mm"))).build());
        parameters.add(NotificationParameter.builder().key("phoneNumber")
            .value("+380638175035, +380931038987").build());

        parameters.forEach(parameter -> parameter.setUserNotification(userNotification));
        when(notificationParameterRepository.saveAll(new HashSet<>(parameters))).thenReturn(parameters);

        notificationService.notifyCourierItineraryFormed(order);

        verify(userNotificationRepository).save(any());
        verify(notificationParameterRepository).saveAll(new HashSet<>(parameters));

    }

    @Test
    void testNotifyInactiveAccounts() {
        User user = User.builder().id(42L).build();
        User user1 = User.builder().id(43L).build();
        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.LETS_STAY_CONNECTED);
        notification.setUser(user);
        notification.setNotificationTime(LocalDateTime.now(fixedClock).minusWeeks(2));

        when(userRepository
            .getAllInactiveUsers(LocalDate.now(fixedClock).minusYears(1), LocalDate.now(fixedClock).minusMonths(2)))
                .thenReturn(List.of(user, user1));
        when(userNotificationRepository
            .findTop1UserNotificationByUserAndNotificationTypeOrderByNotificationTimeDesc(user,
                NotificationType.LETS_STAY_CONNECTED))
                    .thenReturn(Optional.of(notification));
        when(userNotificationRepository
            .findTop1UserNotificationByUserAndNotificationTypeOrderByNotificationTimeDesc(user1,
                NotificationType.LETS_STAY_CONNECTED))
                    .thenReturn(Optional.empty());
        when(userNotificationRepository.save(any())).thenReturn(notification);

        notificationService.notifyInactiveAccounts();

        verify(userNotificationRepository, times(2)).save(any());
    }
}
