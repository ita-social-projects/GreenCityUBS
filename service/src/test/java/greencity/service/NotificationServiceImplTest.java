package greencity.service;

import com.google.common.util.concurrent.MoreExecutors;
import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.NotificationShortDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.entity.enums.NotificationType;
import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.enums.PaymentStatus;
import greencity.entity.notifications.NotificationParameter;
import greencity.entity.notifications.UserNotification;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.exceptions.NotFoundException;
import greencity.repository.*;
import greencity.service.notification.AbstractNotificationProvider;
import greencity.service.ubs.ViberService;
import greencity.ubstelegrambot.TelegramService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import static greencity.ModelUtils.*;
import static greencity.entity.enums.NotificationReceiverType.SITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private NotificationTemplateRepository templateRepository;

    @Mock
    private TelegramService telegramService;

    @Mock
    private ViberService viberService;

    @Mock
    private Clock clock;

    @Mock
    private ExecutorService executorService;

    @Spy
    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Clock fixedClock;

    ExecutorService mockExecutor = MoreExecutors.newDirectExecutorService();

    @Nested
    class ClockNotification {
        @BeforeEach
        public void setUp() {
            MockitoAnnotations.initMocks(this);

            fixedClock = Clock.fixed(LOCAL_DATE_TIME.toInstant(ZoneOffset.ofHours(0)), ZoneId.systemDefault());
            lenient().doReturn(fixedClock.instant()).when(clock).instant();
            lenient().doReturn(fixedClock.getZone()).when(clock).getZone();
        }

        @Test
        void testNotifyUnpaidOrders() {
            List<Order> orders = List.of(
                Order.builder().id(1L).user(getUser()).orderPaymentStatus(OrderPaymentStatus.UNPAID)
                    .orderDate(LocalDateTime.now(fixedClock).minusDays(3))
                    .build(),
                Order.builder().id(2L).user(getUser())
                    .orderPaymentStatus(OrderPaymentStatus.UNPAID)
                    .orderDate(LocalDateTime.now(fixedClock).minusMonths(1)).build(),
                Order.builder().id(3L).user(getUser())
                    .orderPaymentStatus(OrderPaymentStatus.UNPAID)
                    .orderDate(LocalDateTime.now(fixedClock).minusDays(10)).build());

            when(orderRepository.findAllByOrderPaymentStatus(OrderPaymentStatus.UNPAID))
                .thenReturn(orders);

            doReturn(Optional.empty()).when(userNotificationRepository)
                .findLastNotificationByNotificationTypeAndOrderNumber(NotificationType.UNPAID_ORDER.toString(),
                    orders.get(0).getId().toString());

            doReturn(Optional.empty()).when(userNotificationRepository)
                .findLastNotificationByNotificationTypeAndOrderNumber(NotificationType.UNPAID_ORDER.toString(),
                    orders.get(1).getId().toString());

            UserNotification thirdOrderLastNotification = new UserNotification();
            thirdOrderLastNotification.setNotificationType(NotificationType.UNPAID_ORDER);
            thirdOrderLastNotification.setNotificationTime(LocalDateTime.now(fixedClock).minusWeeks(1));

            doReturn(Optional.of(thirdOrderLastNotification)).when(userNotificationRepository)
                .findLastNotificationByNotificationTypeAndOrderNumber(NotificationType.UNPAID_ORDER.toString(),
                    orders.get(2).getId().toString());

            UserNotification created = new UserNotification();
            created.setNotificationType(NotificationType.UNPAID_ORDER);
            created.setNotificationTime(LocalDateTime.now(fixedClock));
            created.setUser(getUser());
            created.setId(1L);

            when(userNotificationRepository.save(any())).thenReturn(created);

            NotificationParameter createdNotificationParameter = NotificationParameter.builder().id(1L)
                .userNotification(created).key("orderNumber")
                .value(orders.get(0).getId().toString()).build();

            createdNotificationParameter.setUserNotification(created);

            when(notificationParameterRepository.save(any())).thenReturn(createdNotificationParameter);

            notificationService.notifyUnpaidOrders();

            verify(notificationParameterRepository, times(3)).save(any());
            verify(userNotificationRepository, times(3)).save(any());
        }

        @Test
        void testNotifyPaidOrder() {
            when(userNotificationRepository.save(TEST_USER_NOTIFICATION)).thenReturn(TEST_USER_NOTIFICATION);

            notificationService.notifyPaidOrder(TEST_ORDER_2);

            verify(userNotificationRepository, times(1)).save(TEST_USER_NOTIFICATION);

        }

        @Test
        @SneakyThrows
        void notifyPaidOrder() {
            Order order = Order.builder().id(1L).build();
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
            PaymentResponseDto dto = PaymentResponseDto.builder().order_id("1_1").build();
            notificationService.notifyPaidOrder(dto);
            verify(notificationService).notifyPaidOrder(order);
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
            userNotification.setOrder(order);
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
        void testNotifyBonuses() {
            when(userNotificationRepository.save(TEST_USER_NOTIFICATION_2)).thenReturn(TEST_USER_NOTIFICATION_2);

            TEST_NOTIFICATION_PARAMETER_SET
                .forEach(parameter -> parameter.setUserNotification(TEST_USER_NOTIFICATION_2));

            when(notificationParameterRepository.saveAll(TEST_NOTIFICATION_PARAMETER_SET))
                .thenReturn(new LinkedList<>(TEST_NOTIFICATION_PARAMETER_SET));

            notificationService.notifyBonuses(TEST_ORDER_3, 2L);

            verify(userNotificationRepository).save(any());
            verify(notificationParameterRepository).saveAll(TEST_NOTIFICATION_PARAMETER_SET);
        }

        @Test
        void testNotifyBonusesFromCanceledOrder() {
            when(userNotificationRepository.save(TEST_USER_NOTIFICATION_5)).thenReturn(TEST_USER_NOTIFICATION_5);

            TEST_NOTIFICATION_PARAMETER_SET2
                .forEach(parameter -> parameter.setUserNotification(TEST_USER_NOTIFICATION_5));

            when(notificationParameterRepository.saveAll(TEST_NOTIFICATION_PARAMETER_SET2))
                .thenReturn(new LinkedList<>(TEST_NOTIFICATION_PARAMETER_SET2));

            notificationService.notifyBonusesFromCanceledOrder(TEST_ORDER_5);

            verify(userNotificationRepository).save(any());
            verify(notificationParameterRepository).saveAll(TEST_NOTIFICATION_PARAMETER_SET2);

        }

        @Test
        void testNotifyAddViolation() {
            Violation violation = TEST_VIOLATION.setOrder(TEST_ORDER_4);
            when(violationRepository.findByOrderId(TEST_ORDER_4.getId())).thenReturn(Optional.of(violation));
            when(userNotificationRepository.save(TEST_USER_NOTIFICATION_3)).thenReturn(TEST_USER_NOTIFICATION_3);
            TEST_NOTIFICATION_PARAMETER.setUserNotification(TEST_USER_NOTIFICATION_3);
            when(notificationParameterRepository.saveAll(Collections.singleton(TEST_NOTIFICATION_PARAMETER)))
                .thenReturn(Collections.singletonList(TEST_NOTIFICATION_PARAMETER));
            TEST_NOTIFICATION_PARAMETER.setUserNotification(TEST_USER_NOTIFICATION_3);

            notificationService.notifyAddViolation(TEST_ORDER_4.getId());

            verify(userNotificationRepository).save(any());
            verify(notificationParameterRepository).saveAll(Collections.singleton(TEST_NOTIFICATION_PARAMETER));
        }

        @Test
        void testNotifyInactiveAccounts() {
            AbstractNotificationProvider abstractNotificationProvider =
                Mockito.mock(AbstractNotificationProvider.class);
            NotificationServiceImpl notificationService1 = new NotificationServiceImpl(
                userRepository,
                userNotificationRepository,
                bagRepository,
                orderRepository,
                violationRepository,
                notificationParameterRepository,
                clock,
                List.of(abstractNotificationProvider),
                templateRepository,
                mockExecutor);
            User user = User.builder().id(42L).build();
            User user1 = User.builder().id(43L).build();
            UserNotification notification = new UserNotification();
            notification.setNotificationType(NotificationType.LETS_STAY_CONNECTED);
            notification.setUser(user);
            notification.setNotificationTime(LocalDateTime.now(fixedClock).minusMonths(2));

            when(userNotificationRepository.getUserIdByDateOfLastNotificationAndNotificationType(
                LocalDate.now(clock).minusMonths(2L), NotificationType.LETS_STAY_CONNECTED.toString()))
                    .thenReturn(List.of(11L, 22L));
            when(userRepository.getInactiveUsersByDateOfLastOrder(LocalDate.now(clock).minusMonths(2L)))
                .thenReturn(List.of(user, user1));
            when(userNotificationRepository.save(any())).thenReturn(notification);

            notificationService1.notifyInactiveAccounts();

            verify(userNotificationRepository, times(2)).save(any());
        }

        @Test
        void testNotifyAllHalfPaidPackages() {
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
            notification.setOrder(orders.get(0));
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
    }

    @Test
    void testGetAllNotificationForUser() {
        when(userRepository.findByUuid("Test")).thenReturn(TEST_USER);
        when(userNotificationRepository.findAllByUser(TEST_USER, TEST_PAGEABLE))
            .thenReturn(TEST_PAGE);
        when(templateRepository.findNotificationTemplateByNotificationTypeAndLanguageCodeAndNotificationReceiverType(
            NotificationType.UNPAID_ORDER,
            "ua",
            SITE)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));

        PageableDto<NotificationShortDto> actual = notificationService
            .getAllNotificationsForUser("Test", "ua", TEST_PAGEABLE);

        assertEquals(TEST_DTO, actual);
    }

    @Test
    void getUnreadenNotificationsTest() {
        assertEquals(0, notificationService.getUnreadenNotifications("Test"));
    }

    @Test
    void testGetNotification() {
        when(userNotificationRepository.findById(1L)).thenReturn(Optional.of(TEST_USER_NOTIFICATION_4));
        when(templateRepository.findNotificationTemplateByNotificationTypeAndLanguageCodeAndNotificationReceiverType(
            NotificationType.UNPAID_ORDER,
            "ua",
            SITE)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));

        NotificationDto actual = notificationService.getNotification("test", 1L, "ua");

        assertEquals(TEST_NOTIFICATION_DTO, actual);
    }

    @Test
    void testGetNotificationThrowsException() {
        when(userNotificationRepository.findById(1L)).thenReturn(Optional.of(TEST_USER_NOTIFICATION_4));

        assertThrows(NotFoundException.class,
            () -> notificationService.getNotification("testtest", 1L, "ua"));
    }

    @Test
    void getNotificationViolation() {
        UserNotification notification = createUserNotificationForViolation();
        notification.getUser().setUuid("abc");
        when(userNotificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(templateRepository.findNotificationTemplateByNotificationTypeAndLanguageCodeAndNotificationReceiverType(
            NotificationType.VIOLATION_THE_RULES,
            "ua",
            SITE)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));
        when(violationRepository.findByOrderId(notification.getOrder().getId()))
            .thenReturn(Optional.of(getViolation()));

        NotificationDto actual = notificationService.getNotification("abc", 1L, "ua");

        assertEquals(createViolationNotificationDto(), actual);
    }

    @Test
    void getNotificationViolationNotFoundException() {
        UserNotification notification = createUserNotificationForViolation();
        notification.getUser().setUuid("abc");
        when(userNotificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(templateRepository.findNotificationTemplateByNotificationTypeAndLanguageCodeAndNotificationReceiverType(
            NotificationType.VIOLATION_THE_RULES,
            "ua",
            SITE)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));
        when(violationRepository.findByOrderId(notification.getOrder().getId()))
            .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> notificationService.getNotification("abc", 1L, "ua"));
    }
}
