package greencity.service;

import greencity.dto.NotificationDto;
import greencity.entity.enums.NotificationType;
import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.enums.PaymentStatus;
import greencity.entity.language.Language;
import greencity.entity.notifications.NotificationParameter;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.notifications.UserNotification;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.repository.*;
import greencity.ubstelegrambot.TelegramService;
import greencity.ubsviberbot.ViberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {
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
    public void testNotifyUnpaidOrders() {
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
    public void testNotifyPaidOrder() {
        Order order = Order.builder().id(43L).user(User.builder().id(42L).build())
            .orderPaymentStatus(OrderPaymentStatus.PAID).orderDate(LocalDateTime.now(fixedClock)).build();

        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.ORDER_IS_PAID);
        notification.setUser(order.getUser());

        when(userNotificationRepository.save(notification)).thenReturn(notification);

        notificationService.notifyPaidOrder(order);

        verify(userNotificationRepository, times(1)).save(notification);
    }

    @Test
    public void testNotifyCourierItineraryFormed() {
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
    public void testNotifyBonuses() {
        Long overpayment = 2L;
        Order order = Order.builder().id(45L).user(User.builder().id(42L).build())
            .confirmedQuantity(new HashMap<>())
            .exportedQuantity(new HashMap<>())
            .orderStatus(OrderStatus.ADJUSTMENT)
            .orderPaymentStatus(OrderPaymentStatus.PAID)
            .orderDate(LocalDateTime.now(fixedClock))
            .build();

        Set<NotificationParameter> parameters = new HashSet<>();

        parameters.add(NotificationParameter.builder().key("overpayment")
            .value(String.valueOf(overpayment)).build());
        parameters.add(NotificationParameter.builder().key("realPackageNumber")
            .value(String.valueOf(0)).build());
        parameters.add(NotificationParameter.builder().key("paidPackageNumber")
            .value(String.valueOf(0)).build());

        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.ACCRUED_BONUSES_TO_ACCOUNT);
        userNotification.setUser(order.getUser());

        when(userNotificationRepository.save(userNotification)).thenReturn(userNotification);

        parameters.forEach(parameter -> parameter.setUserNotification(userNotification));

        when(notificationParameterRepository.saveAll(parameters)).thenReturn(new LinkedList<>(parameters));

        notificationService.notifyBonuses(order, overpayment);

        verify(userNotificationRepository).save(any());
        verify(notificationParameterRepository).saveAll(parameters);
    }

    @Test
    public void testNotifyAddViolation() {
        Order order = Order.builder().id(46L).user(User.builder().id(42L).build())
            .orderDate(LocalDateTime.now(fixedClock))
            .build();

        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.VIOLATION_THE_RULES);
        userNotification.setUser(order.getUser());

        NotificationParameter parameter = NotificationParameter.builder()
            .key("violationDescription")
            .value("violation description")
            .build();

        Violation violation = Violation.builder().description("violation description").build();

        when(violationRepository.findByOrderId(order.getId())).thenReturn(Optional.of(violation));

        when(userNotificationRepository.save(userNotification)).thenReturn(userNotification);
        parameter.setUserNotification(userNotification);
        when(notificationParameterRepository.saveAll(Collections.singleton(parameter)))
            .thenReturn(Collections.singletonList(parameter));
        parameter.setUserNotification(userNotification);

        notificationService.notifyAddViolation(order);

        verify(userNotificationRepository).save(any());
        verify(notificationParameterRepository).saveAll(Collections.singleton(parameter));
    }

    @Test
    public void testNotifyInactiveAccounts() {
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

    @Test
    public void testNotifyAllHalfPaidPackages() {
        User user = User.builder().id(42L).build();
        List<Order> orders = List.of(Order.builder().id(47L).user(user)
            .orderDate(LocalDateTime.now(fixedClock))
            .orderPaymentStatus(OrderPaymentStatus.HALF_PAID)
            .payment(Collections.emptyList())
            .build(),
            Order.builder().id(51L).user(user)
                .orderDate(LocalDateTime.now(fixedClock))
                .orderPaymentStatus(OrderPaymentStatus.HALF_PAID)
                .payment(List.of(
                    Payment.builder()
                        .paymentStatus(PaymentStatus.PAID).amount(0L)
                        .build(),
                    Payment.builder()
                        .paymentStatus(PaymentStatus.PAYMENT_REFUNDED).amount(0L)
                        .build()))
                .build());

        when(orderRepository.findAllByOrderPaymentStatus(OrderPaymentStatus.HALF_PAID))
            .thenReturn(orders);

        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.UNPAID_PACKAGE);
        notification.setUser(User.builder().id(42L).build());
        notification.setNotificationTime(LocalDateTime.now(fixedClock).minusWeeks(2));

        when(userNotificationRepository.findLastNotificationByNotificationTypeAndOrderNumber(
            NotificationType.UNPAID_PACKAGE.toString(),
            orders.get(0).getId().toString())).thenReturn(Optional.of(notification));

        when(userNotificationRepository.findLastNotificationByNotificationTypeAndOrderNumber(
            NotificationType.UNPAID_PACKAGE.toString(),
            orders.get(1).getId().toString())).thenReturn(Optional.empty());

        Set<NotificationParameter> parameters = new HashSet<>();

        when(bagRepository.findBagByOrderId(any())).thenReturn(Collections.emptyList());

        long amountToPay = 0L;

        parameters.add(NotificationParameter.builder().key("amountToPay")
            .value(String.format("%.2f", (double) amountToPay)).build());
        parameters.add(NotificationParameter.builder().key("orderNumber")
            .value(orders.get(0).getId().toString()).build());

        when(userNotificationRepository.save(any())).thenReturn(notification);
        parameters.forEach(parameter -> parameter.setUserNotification(notification));
        when(notificationParameterRepository.saveAll(parameters)).thenReturn(new ArrayList<>(parameters));

        notificationService.notifyAllHalfPaidPackages();

        verify(userNotificationRepository, times(2)).save(notification);
        verify(notificationParameterRepository, times(2)).saveAll(any());
    }

    @Test
    public void testGetAllNotificationsForUser() {
        User user = User.builder().uuid("123").id(42L).build();

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);

        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.LETS_STAY_CONNECTED);
        notification.setUser(user);
        notification.setParameters(Collections.singleton(new NotificationParameter("text", "of notification")));

        when(userNotificationRepository.findAllByUser(user)).thenReturn(Collections.singletonList(notification));

        String language = "en";
        NotificationTemplate template = new NotificationTemplate();
        template.setNotificationType(NotificationType.LETS_STAY_CONNECTED);
        template.setId(20L);
        template.setLanguage(Language.builder().code(language).build());
        template.setTitle("Title");
        template.setBody("Body ${text}");

        when(notificationTemplateRepository
            .findNotificationTemplateByNotificationTypeAndLanguageCode(
                notification.getNotificationType(),
                language)).thenReturn(Optional.of(template));

        List<NotificationDto> expected = Collections.singletonList(
            new NotificationDto("Title", "Body of notification"));

        List<NotificationDto> actual = notificationService.getAllNotificationsForUser("123", "en");

        assertEquals(expected, actual);
        verify(userRepository).findByUuid(user.getUuid());
        verify(userNotificationRepository).findAllByUser(user);
        verify(notificationTemplateRepository)
            .findNotificationTemplateByNotificationTypeAndLanguageCode(notification.getNotificationType(),
                language);
    }

}
