package greencity.service.notification;

import com.google.common.util.concurrent.MoreExecutors;
import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.config.InternalUrlConfigProp;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.constant.constant.TelegramBotConstants;
import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.NotificationFullDto;
import greencity.dto.notification.NotificationShortDto;
import greencity.dto.notification.ScheduledEmailMessage;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.pageble.PageableAdvancedDto;
import greencity.dto.user.UserProfileDto;
import greencity.entity.order.Bag;
import greencity.entity.notifications.NotificationParameter;
import greencity.entity.notifications.UserNotification;
import greencity.entity.order.Event;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.enums.*;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.filters.UserSpecification;
import greencity.repository.NotificationParameterRepository;
import greencity.repository.NotificationTemplateRepository;
import greencity.repository.OrderRepository;
import greencity.repository.UserNotificationRepository;
import greencity.repository.UserRepository;
import greencity.repository.ViolationRepository;
import greencity.service.ubs.OrderBagService;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import greencity.service.ubs.TelegramBotResponseService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import static greencity.ModelUtils.TEST_USER_DEACTIVATED;
import static greencity.ModelUtils.createTestOrder;
import static greencity.ModelUtils.getUserProfileDto;
import static greencity.constant.OrderHistory.ADD_VIOLATION_UK;
import static greencity.constant.OrderHistory.CHANGES_VIOLATION_UK;
import static greencity.constant.OrderHistory.DELETE_VIOLATION_UK;
import static greencity.constant.OrderHistory.ORDER_ADJUSTMENT_UK;
import static greencity.constant.OrderHistory.ORDER_CONFIRMED_UK;
import static greencity.constant.OrderHistory.ORDER_FORMED_UK;
import static greencity.constant.OrderHistory.ORDER_NOT_TAKEN_OUT_UK;
import static greencity.constant.OrderHistory.ORDER_ON_THE_ROUTE_UK;
import static greencity.ModelUtils.TEST_NOTIFICATION_FULL_DTO_PAGEABLE;
import static greencity.ModelUtils.TEST_NOTIFICATION_FULL_DTO_PAGEABLE_2;
import static greencity.ModelUtils.TEST_UUID;
import static greencity.ModelUtils.TEST_PAGEABLE_ADVANCED_DTO;
import static greencity.ModelUtils.TEST_NOTIFICATION_DTO;
import static greencity.ModelUtils.TEST_NOTIFICATION_PARAMETER_SET;
import static greencity.ModelUtils.TEST_NOTIFICATION_PARAMETER_SET2;
import static greencity.ModelUtils.TEST_NOTIFICATION_TEMPLATE;
import static greencity.ModelUtils.TEST_NOTIFICATION_TEMPLATE_2;
import static greencity.ModelUtils.TEST_ORDER_3;
import static greencity.ModelUtils.TEST_ORDER_4;
import static greencity.ModelUtils.TEST_ORDER_5;
import static greencity.ModelUtils.TEST_PAGE;
import static greencity.ModelUtils.TEST_PAGEABLE;
import static greencity.ModelUtils.TEST_PAYMENT_LIST;
import static greencity.ModelUtils.TEST_USER;
import static greencity.ModelUtils.TEST_USER_NOTIFICATION;
import static greencity.ModelUtils.TEST_USER_NOTIFICATION_2;
import static greencity.ModelUtils.TEST_USER_NOTIFICATION_3;
import static greencity.ModelUtils.TEST_USER_NOTIFICATION_4;
import static greencity.ModelUtils.TEST_USER_NOTIFICATION_5;
import static greencity.ModelUtils.TEST_USER_NOTIFICATION_6;
import static greencity.ModelUtils.TEST_USER_NOTIFICATION_7;
import static greencity.ModelUtils.TEST_VIOLATION;
import static greencity.ModelUtils.getNotifyInternallyFormedOrder;
import static greencity.ModelUtils.createUserNotificationForViolationWithParameters;
import static greencity.ModelUtils.createViolationNotificationDto;
import static greencity.ModelUtils.getBag1list;
import static greencity.ModelUtils.getBag4list;
import static greencity.ModelUtils.getActiveCertificateWith10Points;
import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.getViolation;
import static greencity.enums.NotificationReceiverType.SITE;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(1994, 3, 28, 15, 10);
    private static final String ORDER_NUMBER_KEY = "orderNumber";
    private static final String AMOUNT_TO_PAY_KEY = "amountToPay";
    private static final String PAY_BUTTON = "payButton";
    private static final String VIOLATION_DESCRIPTION = "violationDescription";
    private static final String DATE_KEY = "date";
    private static final String START_TIME_KEY = "startTime";
    private static final String END_TIME_KEY = "endTime";
    private static final String PHONE_NUMBER_KEY = "phoneNumber";
    private static final String CUSTOMER = "customerName";
    private static final String PAYMENT_LINK = "https://pay.wayforpay.ua/2412255Qb57omFE7dAjC";
    private static final String USERNAME = "John Smith";
    private static final String USER_EMAIL = "test@some.com";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRemoteClient userRemoteClient;

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @Mock
    private NotificationParameterRepository notificationParameterRepository;

    @Mock
    private ViolationRepository violationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private Clock clock;

    @Mock
    private ExecutorService executorService;

    @Mock
    private Order mockOrder;

    @Mock
    private UserNotification mockUserNotification;

    @Spy
    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private InternalUrlConfigProp internalUrlConfigProp;

    private Clock fixedClock;

    ExecutorService mockExecutor = MoreExecutors.newDirectExecutorService();
    @Mock
    private OrderBagService orderBagService;

    @Mock
    private TelegramBotResponseService telegramBotResponseService;

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(LOCAL_DATE_TIME.toInstant(ZoneOffset.ofHours(0)), ZoneId.systemDefault());
        lenient().doReturn(fixedClock.instant()).when(clock).instant();
        lenient().doReturn(fixedClock.getZone()).when(clock).getZone();
    }

    @Nested
    class ClockNotification {
        @Test
        void testNotifyUnpaidOrders() {
            List<Order> orders = List.of(
                Order.builder().id(1L).user(getUser()).orderPaymentStatus(OrderPaymentStatus.UNPAID)
                    .orderDate(LocalDateTime.now(fixedClock).minusDays(3))
                    .exportedQuantity(new HashMap<>())
                    .confirmedQuantity(new HashMap<>())
                    .amountOfBagsOrdered(new HashMap<>())
                    .build(),
                Order.builder().id(2L).user(getUser())
                    .orderPaymentStatus(OrderPaymentStatus.UNPAID)
                    .orderDate(LocalDateTime.now(fixedClock).minusMonths(1))
                    .exportedQuantity(new HashMap<>())
                    .confirmedQuantity(new HashMap<>())
                    .amountOfBagsOrdered(new HashMap<>())
                    .build(),
                Order.builder().id(3L).user(getUser())
                    .orderPaymentStatus(OrderPaymentStatus.UNPAID)
                    .orderDate(LocalDateTime.now(fixedClock).minusDays(10))
                    .exportedQuantity(new HashMap<>())
                    .confirmedQuantity(new HashMap<>())
                    .amountOfBagsOrdered(new HashMap<>())
                    .build());

            when(orderRepository.findAllByOrderStatusNotAndOrderPaymentStatus(OrderStatus.CANCELED,
                OrderPaymentStatus.UNPAID))
                .thenReturn(orders);

            doReturn(Optional.empty()).when(userNotificationRepository)
                .findFirstByOrderIdAndNotificationTypeInOrderByNotificationTimeDesc(orders.get(0).getId(),
                    NotificationType.UNPAID_ORDER);

            doReturn(Optional.empty()).when(userNotificationRepository)
                .findFirstByOrderIdAndNotificationTypeInOrderByNotificationTimeDesc(orders.get(1).getId(),
                    NotificationType.UNPAID_ORDER);

            UserNotification thirdOrderLastNotification = new UserNotification();
            thirdOrderLastNotification.setNotificationType(NotificationType.UNPAID_ORDER);
            thirdOrderLastNotification.setNotificationTime(LocalDateTime.now(fixedClock).minusWeeks(1));

            doReturn(Optional.of(thirdOrderLastNotification)).when(userNotificationRepository)
                .findFirstByOrderIdAndNotificationTypeInOrderByNotificationTimeDesc(orders.get(2).getId(),
                    NotificationType.UNPAID_ORDER);

            UserNotification created = new UserNotification();
            created.setNotificationType(NotificationType.UNPAID_ORDER);
            created.setNotificationTime(LocalDateTime.now(fixedClock));
            created.setUser(getUser());
            created.setId(1L);
            created.setOrder(orders.getFirst());

            when(userNotificationRepository.save(any())).thenReturn(created);

            Set<NotificationParameter> notificationParameters = Set.of(
                NotificationParameter.builder().id(1L)
                    .userNotification(created).key("orderNumber")
                    .value(orders.getFirst().getId().toString()).build(),
                NotificationParameter.builder().id(2L)
                    .userNotification(created).key("amountToPay")
                    .value("10000").build(),
                NotificationParameter.builder().id(3L)
                    .userNotification(created).key("payButton")
                    .value(PAYMENT_LINK).build());
            List<NotificationParameter> notificationParameterList = new ArrayList<>(notificationParameters);
            when(notificationParameterRepository.saveAll(any())).thenReturn(notificationParameterList);
            when(userNotificationRepository.findUserNotificationByOrderAndNotificationType(any(Order.class),
                any(NotificationType.class))).thenReturn(Optional.of(created));
            when(notificationParameterRepository
                .findNotificationParameterByUserNotification(any(UserNotification.class)))
                .thenReturn(Optional.of(notificationParameters));

            notificationService.notifyUnpaidOrders();

            verify(notificationParameterRepository, times(3)).saveAll(any());
            verify(userNotificationRepository, times(3)).save(any());
        }

        @Test
        void testNotifyUnpaidOrdersWhenOrderDoesNotNeedNewNotification() {
            Order order = Order.builder()
                .id(1L)
                .user(getUser())
                .orderPaymentStatus(OrderPaymentStatus.UNPAID)
                .orderDate(LocalDateTime.now(fixedClock).minusDays(2))
                .build();

            when(orderRepository
                .findAllByOrderStatusNotAndOrderPaymentStatus(OrderStatus.CANCELED, OrderPaymentStatus.UNPAID))
                .thenReturn(List.of(order));
            when(userNotificationRepository.findFirstByOrderIdAndNotificationTypeInOrderByNotificationTimeDesc(
                order.getId(), NotificationType.UNPAID_ORDER))
                .thenReturn(Optional.of(new UserNotification()));

            notificationService.notifyUnpaidOrders();

            verify(userNotificationRepository, never()).save(any());
            verify(notificationParameterRepository, never()).saveAll(any());
        }

        @Test
        void testNotifyUnpaidOrdersWhenPaymentLinkNotPresent() {
            Map<Integer, Integer> amountOfBagsOrdered = new HashMap<>();
            amountOfBagsOrdered.put(1, 2);
            Order order = Order.builder()
                .id(1L)
                .user(getUser())
                .orderPaymentStatus(OrderPaymentStatus.UNPAID)
                .orderDate(LocalDateTime.now(fixedClock).minusDays(4))
                .amountOfBagsOrdered(amountOfBagsOrdered)
                .build();
            Bag bag = Bag.builder()
                .id(1)
                .fullPrice(100L)
                .build();

            when(orderRepository.findAllByOrderStatusNotAndOrderPaymentStatus(
                OrderStatus.CANCELED, OrderPaymentStatus.UNPAID))
                .thenReturn(Collections.singletonList(order));
            when(userNotificationRepository.findUserNotificationByOrderAndNotificationType(
                order, NotificationType.UNPAID_ORDER))
                .thenReturn(Optional.empty());
            when(orderBagService.findAllBagsByOrderId(1L)).thenReturn(Collections.singletonList(bag));

            notificationService.notifyUnpaidOrders();

            verify(userNotificationRepository, never()).save(any());
            verify(userNotificationRepository).findUserNotificationByOrderAndNotificationType(
                order, NotificationType.UNPAID_ORDER);
        }

        @Test
        @SneakyThrows
        void testNotifyPaidOrder() {
            Order order = Order.builder().id(1L).user(getUser()).build();
            NotificationParameter orderNumber = NotificationParameter.builder()
                .key("orderNumber")
                .value(order.getId().toString())
                .build();
            when(notificationParameterRepository.saveAll(Set.of(orderNumber))).thenReturn(List.of(orderNumber));
            when(userNotificationRepository.save(any())).thenReturn(TEST_USER_NOTIFICATION);
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            notificationService.notifyPaidOrder(order.getId());

            verify(notificationService).notifyPaidOrder(order.getId());
            verify(userNotificationRepository).save(any(UserNotification.class));
            verify(notificationParameterRepository).saveAll(Set.of(orderNumber));
        }

        @Test
        void testNotifyUnpaidOrderPermanently() {
            long amountToPay = 10000L;
            User user = getUser();

            when(mockOrder.getOrderPaymentStatus()).thenReturn(OrderPaymentStatus.UNPAID);
            when(mockOrder.getId()).thenReturn(1L);
            when(mockOrder.getUser()).thenReturn(user);
            when(userNotificationRepository.save(any(UserNotification.class))).thenReturn(mockUserNotification);
            when(notificationParameterRepository.saveAll(any())).thenAnswer(
                invocation -> new ArrayList<>(invocation.getArgument(0)));
            when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));

            assertDoesNotThrow(() -> notificationService.notifyUnpaidOrderPermanently(
                mockOrder.getId(),
                amountToPay, PaymentSystemResponse.builder()
                    .orderId(1L).link(PAYMENT_LINK).build()));

            verify(userNotificationRepository).save(any(UserNotification.class));
            verify(notificationParameterRepository).saveAll(any());
        }

        @Test
        void testNotifyUnpaidOrderPermanentlyWhenOrderIsPaid() {
            long amountToPay = 10000L;
            Order order = Order.builder()
                .id(1L)
                .orderPaymentStatus(OrderPaymentStatus.PAID)
                .build();

            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            notificationService.notifyUnpaidOrderPermanently(order.getId(), amountToPay,
                PaymentSystemResponse.builder().link(PAYMENT_LINK).build());

            verify(userNotificationRepository, never()).save(any());
            verify(notificationParameterRepository, never()).saveAll(any());
        }

        @Test
        void testNotifyCourierItineraryFormed() {
            Order order = getNotifyInternallyFormedOrder();

            UserNotification userNotification = getInternallyFormedOrderUserNotification(order);
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            List<NotificationParameter> parameters = courierInternallyFormedParameters(order);
            when(userNotificationRepository.save(any())).thenReturn(userNotification);

            parameters.forEach(parameter -> parameter.setUserNotification(userNotification));
            when(notificationParameterRepository.saveAll(new HashSet<>(parameters))).thenReturn(parameters);

            notificationService.notifyCourierItineraryFormed(order.getId());

            verify(userNotificationRepository).save(any());
            verify(notificationParameterRepository).saveAll(new HashSet<>(parameters));
        }

        @Test
        void testNotifyAllCourierItineraryFormed() {
            Order order = getNotifyInternallyFormedOrder();
            List<Order> orders = Collections.singletonList(order);
            UserNotification userNotification = getInternallyFormedOrderUserNotification(order);
            List<NotificationParameter> parameters = courierInternallyFormedParameters(order);

            mockUserNeedNotificationCheck(order, NotificationType.COURIER_ITINERARY_FORMED);
            when(orderRepository.findAllByOrderStatusAndOrderPaymentStatus(
                OrderStatus.ADJUSTMENT, OrderPaymentStatus.PAID)).thenReturn(orders);
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            when(userNotificationRepository.save(any())).thenReturn(userNotification);

            parameters.forEach(parameter -> parameter.setUserNotification(userNotification));
            when(notificationParameterRepository.saveAll(new HashSet<>(parameters))).thenReturn(parameters);

            notificationService.notifyAllCourierItineraryFormed();

            verifyUserNeedNotificationCheck();
            verify(userNotificationRepository).save(any());
            verify(notificationParameterRepository).saveAll(new HashSet<>(parameters));
        }

        private UserNotification getInternallyFormedOrderUserNotification(Order order) {
            UserNotification userNotification = new UserNotification();
            userNotification.setNotificationType(NotificationType.COURIER_ITINERARY_FORMED);
            userNotification.setUser(order.getUser());
            userNotification.setOrder(order);

            return userNotification;
        }

        private List<NotificationParameter> courierInternallyFormedParameters(Order order) {
            List<NotificationParameter> parameters = new LinkedList<>();
            parameters.add(NotificationParameter.builder().key(DATE_KEY)
                .value(order.getDeliverFrom().format(DateTimeFormatter.ofPattern("dd-MM"))).build());
            parameters.add(NotificationParameter.builder().key(START_TIME_KEY)
                .value(order.getDeliverFrom().format(DateTimeFormatter.ofPattern("hh:mm"))).build());
            parameters.add(NotificationParameter.builder().key(END_TIME_KEY)
                .value(order.getDeliverTo().format(DateTimeFormatter.ofPattern("hh:mm"))).build());
            parameters.add(NotificationParameter.builder().key(PHONE_NUMBER_KEY)
                .value("+380638175035, +380931038987").build());
            parameters.add(NotificationParameter.builder().key(ORDER_NUMBER_KEY)
                .value(order.getId().toString()).build());

            return parameters;
        }

        @Test
        void testNotifyBonuses() {
            when(userNotificationRepository.save(TEST_USER_NOTIFICATION_2)).thenReturn(TEST_USER_NOTIFICATION_2);
            when(orderRepository.findById(TEST_ORDER_3.getId())).thenReturn(Optional.of(TEST_ORDER_3));

            TEST_NOTIFICATION_PARAMETER_SET
                .forEach(parameter -> parameter.setUserNotification(TEST_USER_NOTIFICATION_2));

            when(notificationParameterRepository.saveAll(TEST_NOTIFICATION_PARAMETER_SET))
                .thenReturn(new LinkedList<>(TEST_NOTIFICATION_PARAMETER_SET));

            notificationService.notifyBonuses(TEST_ORDER_3.getId(), 2L);

            verify(userNotificationRepository).save(any());
            verify(notificationParameterRepository).saveAll(TEST_NOTIFICATION_PARAMETER_SET);
        }

        @Test
        void testNotifyBonusesWhenOverpaymentZero() {
            Order order = Order.builder().id(1L).build();

            notificationService.notifyBonuses(order.getId(), 0L);

            verify(userNotificationRepository, never()).save(any());
            verify(notificationParameterRepository, never()).saveAll(any());
        }

        @Test
        void testNotifyBonusesFromCanceledOrder() {
            when(userNotificationRepository.save(TEST_USER_NOTIFICATION_5)).thenReturn(TEST_USER_NOTIFICATION_5);

            TEST_NOTIFICATION_PARAMETER_SET2
                .forEach(parameter -> parameter.setUserNotification(TEST_USER_NOTIFICATION_5));

            when(notificationParameterRepository.saveAll(TEST_NOTIFICATION_PARAMETER_SET2))
                .thenReturn(new LinkedList<>(TEST_NOTIFICATION_PARAMETER_SET2));
            when(orderRepository.findById(TEST_ORDER_5.getId())).thenReturn(Optional.of(TEST_ORDER_5));

            notificationService.notifyBonusesFromCanceledOrder(TEST_ORDER_5.getId());

            verify(userNotificationRepository).save(any());
            verify(notificationParameterRepository).saveAll(TEST_NOTIFICATION_PARAMETER_SET2);

        }

        @Test
        void testNotifyBonusesFromCanceledOrderWhenPointsToReturnZero() {
            Order order = Order.builder()
                .id(1L)
                .pointsToUse(0)
                .build();

            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            notificationService.notifyBonusesFromCanceledOrder(order.getId());

            verify(userNotificationRepository, never()).save(any());
            verify(notificationParameterRepository, never()).saveAll(any());
        }

        @Test
        void testNotifyAddViolation() {
            Set<NotificationParameter> parameters = new HashSet<>();
            parameters.add(NotificationParameter.builder()
                .key("violationDescription")
                .value("violation description")
                .build());
            parameters.add(NotificationParameter.builder()
                .key("orderNumber")
                .value("46")
                .build());
            Violation violation = TEST_VIOLATION.setOrder(TEST_ORDER_4);
            when(violationRepository.findActiveViolationByOrderId(TEST_ORDER_4.getId()))
                .thenReturn(Optional.of(violation));
            when(userNotificationRepository.save(TEST_USER_NOTIFICATION_3)).thenReturn(TEST_USER_NOTIFICATION_3);
            parameters.forEach(p -> p.setUserNotification(TEST_USER_NOTIFICATION_3));
            when(notificationParameterRepository.saveAll(parameters)).thenReturn(new LinkedList<>(parameters));

            notificationService.notifyAddViolation(TEST_ORDER_4.getId());

            verify(userNotificationRepository).save(any());
            verify(notificationParameterRepository).saveAll(parameters);
        }

        @Test
        void testNotifyChangedViolation() {
            Set<NotificationParameter> parameters = new HashSet<>();
            parameters.add(NotificationParameter.builder()
                .key("orderNumber")
                .value("46")
                .build());
            Violation violation = TEST_VIOLATION.setOrder(TEST_ORDER_4);
            when(userNotificationRepository.save(TEST_USER_NOTIFICATION_6)).thenReturn(TEST_USER_NOTIFICATION_6);
            parameters.forEach(p -> p.setUserNotification(TEST_USER_NOTIFICATION_6));
            when(notificationParameterRepository.saveAll(parameters)).thenReturn(new LinkedList<>(parameters));
            when(violationRepository.findById(violation.getId())).thenReturn(Optional.of(violation));

            notificationService.notifyChangedViolation(violation.getId(), TEST_ORDER_4.getId());

            verify(userNotificationRepository).save(any());
            verify(notificationParameterRepository).saveAll(parameters);
        }

        @Test
        void testNotifyDeleteViolation() {
            Set<NotificationParameter> parameters = new HashSet<>();
            parameters.add(NotificationParameter.builder()
                .key("orderNumber")
                .value("46")
                .build());
            Violation violation = TEST_VIOLATION.setOrder(TEST_ORDER_4);
            when(violationRepository.findCanceledViolationByOrderId(TEST_ORDER_4.getId()))
                .thenReturn(Optional.of(violation));
            when(userNotificationRepository.save(TEST_USER_NOTIFICATION_7)).thenReturn(TEST_USER_NOTIFICATION_7);
            parameters.forEach(p -> p.setUserNotification(TEST_USER_NOTIFICATION_7));
            when(notificationParameterRepository.saveAll(parameters)).thenReturn(new LinkedList<>(parameters));
            notificationService.notifyDeleteViolation(TEST_ORDER_4.getId());

            verify(userNotificationRepository).save(any());
            verify(notificationParameterRepository).saveAll(parameters);
        }

        @Test
        void testNotifyAllAddedViolations() {
            Order order = TEST_ORDER_4;
            List<Order> orders = Collections.singletonList(order);
            setEventsToOrder(order, ADD_VIOLATION_UK);
            Violation violation = getOrdersViolations(order);
            Set<NotificationParameter> parameters = getNewViolationParameter(violation);

            mockUserNeedNotificationCheck(order, NotificationType.VIOLATION_THE_RULES);
            when(orderRepository.findAllWithEventsByEventNames(ADD_VIOLATION_UK, CHANGES_VIOLATION_UK,
                DELETE_VIOLATION_UK))
                .thenReturn(orders);
            when(violationRepository.findActiveViolationByOrderId(anyLong())).thenReturn(Optional.of(violation));
            mockFillAndSendNotification(parameters, order, NotificationType.VIOLATION_THE_RULES);

            notificationService.notifyAllAddedViolations();

            verify(userNotificationRepository).save(any());
            verify(orderRepository).findAllWithEventsByEventNames(anyString(), anyString(), anyString());
            verify(violationRepository).findActiveViolationByOrderId(anyLong());
            verifyFillAndSendNotification();
        }

        private Violation getOrdersViolations(Order order) {
            return Violation.builder().description("").order(order).build();
        }

        private Set<NotificationParameter> getNewViolationParameter(Violation violation) {
            Set<NotificationParameter> parameters = new HashSet<>();
            parameters.add(NotificationParameter.builder()
                .key(VIOLATION_DESCRIPTION)
                .value(violation.getDescription())
                .build());
            parameters.add(NotificationParameter.builder()
                .key(ORDER_NUMBER_KEY)
                .value(String.valueOf(violation.getOrder().getId()))
                .build());
            return parameters;
        }

        @Test
        void testNotifyAllAddedViolationsWhenThereAreNotJustNewViolations() {
            Order order = TEST_ORDER_4;
            List<Order> orders = Collections.singletonList(order);
            setEventsToOrder(order, ADD_VIOLATION_UK, CHANGES_VIOLATION_UK);

            when(orderRepository.findAllWithEventsByEventNames(ADD_VIOLATION_UK, CHANGES_VIOLATION_UK,
                DELETE_VIOLATION_UK))
                .thenReturn(orders);

            notificationService.notifyAllAddedViolations();

            verify(orderRepository).findAllWithEventsByEventNames(anyString(), anyString(), anyString());
        }

        @Test
        void testNotifyAllChangedViolations() {
            Order order = TEST_ORDER_4;
            List<Order> orders = Collections.singletonList(order);
            setEventsToOrder(order, CHANGES_VIOLATION_UK);
            Set<NotificationParameter> parameters = getViolationParameter(order);

            mockUserNeedNotificationCheck(order, NotificationType.CHANGED_IN_RULE_VIOLATION_STATUS);
            when(orderRepository.findAllWithEventsByEventNames(CHANGES_VIOLATION_UK, DELETE_VIOLATION_UK))
                .thenReturn(orders);
            mockFillAndSendNotification(parameters, order, NotificationType.CHANGED_IN_RULE_VIOLATION_STATUS);

            notificationService.notifyAllChangedViolations();

            verifyUserNeedNotificationCheck();
            verify(orderRepository).findAllWithEventsByEventNames(anyString(), anyString());
            verifyFillAndSendNotification();
        }

        @Test
        void testNotifyAllChangedViolationsWithDeletedEvents() {
            Order order = TEST_ORDER_4;
            List<Order> orders = Collections.singletonList(order);
            setEventsToOrder(order, CHANGES_VIOLATION_UK, DELETE_VIOLATION_UK);

            when(orderRepository.findAllWithEventsByEventNames(CHANGES_VIOLATION_UK, DELETE_VIOLATION_UK))
                .thenReturn(orders);

            notificationService.notifyAllChangedViolations();

            verify(orderRepository).findAllWithEventsByEventNames(anyString(), anyString());
        }

        @Test
        void testNotifyAllCanceledViolations() {
            Order order = TEST_ORDER_4;
            List<Order> orders = Collections.singletonList(order);
            setEventsToOrder(order, DELETE_VIOLATION_UK);
            Set<NotificationParameter> parameters = getViolationParameter(order);

            mockUserNeedNotificationCheck(order, NotificationType.CANCELED_VIOLATION_THE_RULES_BY_THE_MANAGER);
            when(orderRepository.findAllWithEventsByEventNames(DELETE_VIOLATION_UK)).thenReturn(orders);
            mockFillAndSendNotification(parameters, order,
                NotificationType.CANCELED_VIOLATION_THE_RULES_BY_THE_MANAGER);

            notificationService.notifyAllCanceledViolations();

            verifyUserNeedNotificationCheck();
            verify(orderRepository).findAllWithEventsByEventNames(anyString());
            verifyFillAndSendNotification();
        }

        @Test
        void testNotifyAllCanceledViolationsForInactiveUser() {
            Order order = createTestOrder();
            order.getUser().setStatus(UserStatus.DEACTIVATED);
            List<Order> orders = Collections.singletonList(order);
            setEventsToOrder(order, DELETE_VIOLATION_UK);
            mockUserNeedNotificationCheck(order, NotificationType.CANCELED_VIOLATION_THE_RULES_BY_THE_MANAGER);
            when(orderRepository.findAllWithEventsByEventNames(DELETE_VIOLATION_UK)).thenReturn(orders);

            notificationService.notifyAllCanceledViolations();

            verify(userNotificationRepository, never()).save(any());
        }

        private Set<NotificationParameter> getViolationParameter(Order order) {
            Set<NotificationParameter> parameters = new HashSet<>();
            parameters.add(NotificationParameter.builder()
                .key(ORDER_NUMBER_KEY)
                .value(order.getId().toString())
                .build());
            return parameters;
        }

        @Test
        void testNotifyAllDoneOrCanceledUnpaidOrders() {
            Order order = getOrderWithAmountToPay();
            List<Order> orders = Collections.singletonList(order);
            setEventsToOrder(order, ORDER_ADJUSTMENT_UK, ORDER_CONFIRMED_UK, ORDER_ON_THE_ROUTE_UK);
            Set<NotificationParameter> parameters = initialiseNotificationParametersForUnpaidOrder(order);

            mockUserNeedNotificationCheck(order, NotificationType.DONE_OR_CANCELED_UNPAID_ORDER);
            when(orderRepository.findAllByPaymentStatusesAndOrderStatuses(
                List.of(OrderPaymentStatus.UNPAID, OrderPaymentStatus.HALF_PAID),
                List.of(OrderStatus.DONE, OrderStatus.CANCELED))).thenReturn(orders);
            when(orderBagService.findAllBagsByOrderId(any())).thenReturn(getBag4list());
            mockFillAndSendNotification(parameters, order, NotificationType.DONE_OR_CANCELED_UNPAID_ORDER);

            notificationService.notifyAllDoneOrCanceledUnpaidOrders();

            verifyUserNeedNotificationCheck();
            verify(orderRepository).findAllByPaymentStatusesAndOrderStatuses(any(), any());
            verifyFillAndSendNotification();
        }

        @Test
        void testNotifyAllDoneOrCanceledUnpaidOrdersWithoutRequiredEvents() {
            Order order = getOrderWithAmountToPay();
            List<Order> orders = Collections.singletonList(order);
            order.setEvents(Collections.emptyList());

            when(orderRepository.findAllByPaymentStatusesAndOrderStatuses(
                List.of(OrderPaymentStatus.UNPAID, OrderPaymentStatus.HALF_PAID),
                List.of(OrderStatus.DONE, OrderStatus.CANCELED))).thenReturn(orders);

            notificationService.notifyAllDoneOrCanceledUnpaidOrders();

            verify(orderRepository).findAllByPaymentStatusesAndOrderStatuses(any(), any());
        }

        @Test
        void testNotifyAllHalfPaidOrdersWithStatusBroughtByHimself() {
            Order order = getOrderWithAmountToPay();
            List<Order> orders = Collections.singletonList(order);
            Set<NotificationParameter> parameters = initialiseNotificationParametersForUnpaidOrder(order);

            mockUserNeedNotificationCheck(order, NotificationType.HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF);
            when(orderRepository.findAllByOrderStatusAndOrderPaymentStatus(OrderStatus.BROUGHT_IT_HIMSELF,
                OrderPaymentStatus.HALF_PAID)).thenReturn(orders);
            when(orderBagService.findAllBagsByOrderId(any())).thenReturn(getBag4list());
            mockFillAndSendNotification(parameters, order,
                NotificationType.HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF);

            notificationService.notifyAllHalfPaidOrdersWithStatusBroughtByHimself();

            verifyUserNeedNotificationCheck();
            verify(orderRepository).findAllByOrderStatusAndOrderPaymentStatus(any(), any());
            verifyFillAndSendNotification();
        }

        @Test
        void testNotifyAllChangedOrderStatuses() {
            Order order = getOrderWithAmountToPay();
            setEventsToOrder(order, ORDER_NOT_TAKEN_OUT_UK, ADD_VIOLATION_UK);
            List<Order> orders = Collections.singletonList(order);
            Set<NotificationParameter> parameters = initialiseNotificationParametersForUnpaidOrder(order);

            when(orderRepository.findAllByOrderStatusWithEvents(OrderStatus.BROUGHT_IT_HIMSELF)).thenReturn(orders);
            mockUserNeedNotificationCheck(order, NotificationType.ORDER_STATUS_CHANGED);
            when(orderBagService.findAllBagsByOrderId(any())).thenReturn(getBag4list());
            mockFillAndSendNotification(parameters, order, NotificationType.ORDER_STATUS_CHANGED);

            notificationService.notifyAllChangedOrderStatuses();

            verifyUserNeedNotificationCheck();
            verify(orderRepository).findAllByOrderStatusWithEvents(any());
            verifyFillAndSendNotification();
        }

        @Test
        void testNotifyAllChangedOrderStatusesWithUnacceptableEvents() {
            Order order = getOrderWithAmountToPay();
            setEventsToOrder(order, ORDER_ADJUSTMENT_UK, ORDER_CONFIRMED_UK);
            List<Order> orders = Collections.singletonList(order);

            when(orderRepository.findAllByOrderStatusWithEvents(OrderStatus.BROUGHT_IT_HIMSELF)).thenReturn(orders);

            notificationService.notifyAllChangedOrderStatuses();

            verify(orderRepository).findAllByOrderStatusWithEvents(any());
        }

        @ParameterizedTest
        @MethodSource("correctArguments")
        void testNotifyUnpaidPackages(OrderStatus orderStatus, List<String> eventNames) {
            Order order = getOrderWithAmountToPay();
            order.setOrderStatus(orderStatus);
            setEventsToOrder(order, eventNames);
            List<Order> orders = Collections.singletonList(order);
            Set<NotificationParameter> parameters = initialiseNotificationParametersForUnpaidOrder(order);

            when(orderRepository.findAllByOrderPaymentStatusWithEvents(OrderPaymentStatus.HALF_PAID))
                .thenReturn(orders);
            mockUserNeedNotificationCheck(order, NotificationType.UNPAID_PACKAGE);
            when(orderBagService.findAllBagsByOrderId(any())).thenReturn(getBag4list());
            mockFillAndSendNotification(parameters, order, NotificationType.UNPAID_PACKAGE);

            notificationService.notifyUnpaidPackages();

            verifyUserNeedNotificationCheck();
            verify(orderRepository).findAllByOrderPaymentStatusWithEvents(any());
            verifyFillAndSendNotification();
        }

        private static Stream<Arguments> correctArguments() {
            return Stream.of(
                Arguments.of(OrderStatus.CONFIRMED,
                    asList(ORDER_CONFIRMED_UK, ORDER_ON_THE_ROUTE_UK, ORDER_NOT_TAKEN_OUT_UK)),
                Arguments.of(OrderStatus.DONE, Collections.singletonList(ORDER_CONFIRMED_UK)),
                Arguments.of(OrderStatus.CANCELED, asList(ORDER_CONFIRMED_UK, ORDER_ON_THE_ROUTE_UK)));
        }

        @ParameterizedTest
        @MethodSource("wrongForUnpaidOrderStatusesProvider")
        void testNotifyUnpaidPackagesWhenOrderHasUnacceptableStatusesAndEvents(OrderStatus status) {
            Order order = getOrderWithAmountToPay();
            order.setOrderStatus(status);
            setEventsToOrder(order, ORDER_CONFIRMED_UK, ORDER_ON_THE_ROUTE_UK, ORDER_NOT_TAKEN_OUT_UK);
            List<Order> orders = Collections.singletonList(order);

            when(orderRepository.findAllByOrderPaymentStatusWithEvents(OrderPaymentStatus.HALF_PAID))
                .thenReturn(orders);

            notificationService.notifyUnpaidPackages();

            verify(orderRepository).findAllByOrderPaymentStatusWithEvents(any());
        }

        @Test
        void testNotifyAllOrdersWithIncreasedTariffPrice() {
            Order order = getOrderWithAmountToPay();
            List<Order> orders = Collections.singletonList(order);
            Set<NotificationParameter> parameters = initialiseNotificationParametersForUnpaidOrder(order);

            when(orderRepository.findAllUnpaidOrdersWithUsersByBagId(anyInt())).thenReturn(orders);
            when(orderBagService.findAllBagsByOrderId(any())).thenReturn(getBag4list());
            mockFillAndSendNotification(parameters, order, NotificationType.TARIFF_PRICE_WAS_CHANGED);

            notificationService.notifyAllOrdersWithIncreasedTariffPrice(anyInt());
            verify(orderRepository).findAllUnpaidOrdersWithUsersByBagId(any());
            verifyFillAndSendNotification();
        }

        private static Stream<OrderStatus> wrongForUnpaidOrderStatusesProvider() {
            return Stream.of(OrderStatus.DONE, OrderStatus.CANCELED);
        }

        private void setEventsToOrder(Order order, String... eventNames) {
            List<Event> events = Stream.of(eventNames)
                .map(e -> Event.builder().eventNameUk(e).build())
                .toList();
            order.setEvents(events);
        }

        private void setEventsToOrder(Order order, List<String> eventNames) {
            List<Event> events = eventNames.stream()
                .map(e -> Event.builder().eventNameUk(e).build())
                .toList();
            order.setEvents(events);
        }

        private Order getOrderWithAmountToPay() {
            Order order = TEST_ORDER_4;
            order.setConfirmedQuantity(Collections.singletonMap(1, 1));
            order.setExportedQuantity(Collections.emptyMap());
            order.setPayment(TEST_PAYMENT_LIST);
            order.setPointsToUse(0);
            return order;
        }

        private Set<NotificationParameter> initialiseNotificationParametersForUnpaidOrder(Order order) {
            Set<NotificationParameter> parameters = new HashSet<>();

            parameters.add(NotificationParameter.builder()
                .key(AMOUNT_TO_PAY_KEY)
                .value("10")
                .build());

            parameters.add(NotificationParameter.builder()
                .key(ORDER_NUMBER_KEY)
                .value(order.getId().toString())
                .build());

            parameters.add(NotificationParameter.builder()
                .key(PAY_BUTTON)
                .value("url")
                .build());

            return parameters;
        }

        private void mockUserNeedNotificationCheck(Order order, NotificationType notificationType) {
            when(userNotificationRepository
                .findFirstByOrderIdAndNotificationTypeInOrderByNotificationTimeDesc(order.getId(),
                    notificationType)).thenReturn(Optional.empty());
        }

        private void mockFillAndSendNotification(Set<NotificationParameter> parameters, Order order,
            NotificationType notificationType) {
            UserNotification userNotification = new UserNotification();
            userNotification.setNotificationType(notificationType);
            userNotification.setUser(order.getUser());
            userNotification.setOrder(order);
            parameters.forEach(p -> p.setUserNotification(userNotification));

            when(userNotificationRepository.save(any())).thenReturn(userNotification);
            when(notificationParameterRepository.saveAll(any())).thenReturn(new LinkedList<>(parameters));
        }

        private void verifyFillAndSendNotification() {
            verify(userNotificationRepository).save(any());
            verify(notificationParameterRepository).saveAll(any());
        }

        private void verifyUserNeedNotificationCheck() {
            verify(userNotificationRepository)
                .findFirstByOrderIdAndNotificationTypeInOrderByNotificationTimeDesc(anyLong(), any());
        }

        @Test
        void testNotifyCustom() {
            var user = getUser();
            var templateId = 1L;
            var userNotification = new UserNotification();
            userNotification.setNotificationType(NotificationType.CUSTOM);
            userNotification.setUser(user);
            userNotification.setTemplateId(templateId);

            when(userRepository.findAll(any(UserSpecification.class))).thenReturn(Collections.singletonList(user));
            when(userNotificationRepository.save(any())).thenReturn(userNotification);

            notificationService.notifyCustom(templateId, UserCategory.USERS_WITH_ORDERS_MADE_LESS_THAN_3_MONTHS);

            verify(userNotificationRepository).save(any());
            verify(userRepository).findAll(any(UserSpecification.class));
        }

        @Test
        void testNotifyCustomForInactiveUser() {
            List<User> userList = List.of(TEST_USER_DEACTIVATED);

            when(userRepository.findAll(any(UserSpecification.class))).thenReturn(userList);

            notificationService.notifyCustom(1L, UserCategory.USERS_WITH_ORDERS_MADE_LESS_THAN_3_MONTHS);

            verify(userNotificationRepository, never()).save(any());

        }

        @Test
        void testNotifyInactiveAccounts() {
            AbstractNotificationProvider abstractNotificationProvider =
                mock(AbstractNotificationProvider.class);
            NotificationServiceImpl notificationService1 = new NotificationServiceImpl(
                userRepository,
                userNotificationRepository,
                orderRepository,
                violationRepository,
                notificationParameterRepository,
                userRemoteClient,
                telegramBotResponseService,
                clock,
                List.of(abstractNotificationProvider),
                templateRepository,
                mockExecutor,
                internalUrlConfigProp,
                orderBagService,
                modelMapper);
            UserProfileDto profile = getUserProfileDto();
            User user = User.builder().id(42L).recipientEmail(profile.getRecipientEmail()).build();
            UserNotification notification = new UserNotification();
            notification.setNotificationType(NotificationType.LETS_STAY_CONNECTED);
            notification.setUser(user);
            notification.setNotificationTime(LocalDateTime.now(fixedClock).minusMonths(2));

            when(userNotificationRepository.getUserIdByDateOfLastNotificationAndNotificationType(
                LocalDate.now(clock).minusMonths(2L), NotificationType.LETS_STAY_CONNECTED.toString()))
                .thenReturn(List.of(11L, 22L));
            when(userRepository.getInactiveUsersByDateOfLastOrder(LocalDate.now(clock).minusMonths(2L)))
                .thenReturn(List.of(user));
            when(userNotificationRepository.save(any())).thenReturn(notification);
            when(userRepository.findByRecipientEmail(anyString())).thenReturn(Optional.of(user));
            when(modelMapper.map(user, UserProfileDto.class)).thenReturn(profile);

            notificationService1.notifyInactiveAccounts();

            verify(userNotificationRepository, times(1)).save(any());
        }

        @Test
        void testNotifyInactiveAccountsWhenExecutionException() throws Exception {
            when(executorService.invokeAll(any())).thenReturn(List.of(mock(Future.class)));
            when(executorService.invokeAll(any()).get(0).get())
                .thenThrow(new ExecutionException("Test exception", null));

            assertDoesNotThrow(() -> notificationService.notifyInactiveAccounts());

            verify(executorService, times(2)).invokeAll(any());
            verify(userNotificationRepository, never()).save(any());
        }

        @Test
        void testNotifyAllHalfPaidPackages() {
            User user = getUser();
            List<Order> orders = List.of(Order.builder().id(47L).user(user)
                .orderDate(LocalDateTime.now(fixedClock))
                .orderPaymentStatus(OrderPaymentStatus.HALF_PAID)
                .payment(Collections.emptyList())
                .certificates(Collections.emptySet())
                .amountOfBagsOrdered(Collections.singletonMap(1, 3))
                .exportedQuantity(Collections.emptyMap())
                .confirmedQuantity(Collections.singletonMap(1, 3))
                .pointsToUse(50)
                .payment(List.of(
                    Payment.builder()
                        .paymentStatus(PaymentStatus.PAID).amount(5000L)
                        .build(),
                    Payment.builder()
                        .paymentStatus(PaymentStatus.UNPAID).amount(0L)
                        .build()))
                .build(),
                Order.builder().id(53L).user(user)
                    .orderDate(LocalDateTime.now(fixedClock))
                    .orderPaymentStatus(OrderPaymentStatus.HALF_PAID)
                    .certificates(Collections.singleton(getActiveCertificateWith10Points()))
                    .amountOfBagsOrdered(Collections.singletonMap(1, 3))
                    .exportedQuantity(Collections.singletonMap(1, 3))
                    .confirmedQuantity(Collections.singletonMap(1, 3))
                    .pointsToUse(40)
                    .payment(List.of(
                        Payment.builder()
                            .paymentStatus(PaymentStatus.PAID).amount(5000L)
                            .build(),
                        Payment.builder()
                            .paymentStatus(PaymentStatus.PAYMENT_REFUNDED).amount(0L)
                            .build()))
                    .build(),
                Order.builder().id(51L).user(user)
                    .orderDate(LocalDateTime.now(fixedClock))
                    .orderPaymentStatus(OrderPaymentStatus.HALF_PAID)
                    .certificates(Collections.emptySet())
                    .amountOfBagsOrdered(Collections.singletonMap(1, 3))
                    .exportedQuantity(Collections.emptyMap())
                    .confirmedQuantity(Collections.emptyMap())
                    .pointsToUse(0)
                    .payment(List.of(
                        Payment.builder()
                            .paymentStatus(PaymentStatus.PAID).amount(10000L)
                            .build(),
                        Payment.builder()
                            .paymentStatus(PaymentStatus.PAYMENT_REFUNDED).amount(0L)
                            .build()))
                    .build());

            when(orderRepository.findAllByOrderPaymentStatus(OrderPaymentStatus.HALF_PAID))
                .thenReturn(orders);
            orders.forEach(o -> when(orderRepository.findById(o.getId())).thenReturn(Optional.of(o)));

            UserNotification notification = new UserNotification();
            notification.setNotificationType(NotificationType.UNPAID_PACKAGE);
            notification.setUser(user);
            notification.setOrder(orders.getFirst());
            notification.setNotificationTime(LocalDateTime.now(fixedClock).minusWeeks(2));

            when(userNotificationRepository.findFirstByOrderIdAndNotificationTypeInOrderByNotificationTimeDesc(
                orders.get(0).getId(),
                NotificationType.UNPAID_PACKAGE,
                NotificationType.HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF,
                NotificationType.DONE_OR_CANCELED_UNPAID_ORDER)).thenReturn(Optional.of(notification));

            when(userNotificationRepository.findFirstByOrderIdAndNotificationTypeInOrderByNotificationTimeDesc(
                orders.get(1).getId(),
                NotificationType.UNPAID_PACKAGE,
                NotificationType.HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF,
                NotificationType.DONE_OR_CANCELED_UNPAID_ORDER)).thenReturn(Optional.empty());

            when(userNotificationRepository.findFirstByOrderIdAndNotificationTypeInOrderByNotificationTimeDesc(
                orders.get(2).getId(),
                NotificationType.UNPAID_PACKAGE,
                NotificationType.HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF,
                NotificationType.DONE_OR_CANCELED_UNPAID_ORDER)).thenReturn(Optional.empty());

            Set<NotificationParameter> parameters = new HashSet<>();

            long amountToPay = 4400L;
            parameters.add(NotificationParameter.builder().key("amountToPay")
                .value(String.format("%.2f", (double) amountToPay)).build());
            parameters.add(NotificationParameter.builder().key("orderNumber")
                .value(orders.getFirst().getId().toString()).build());

            when(orderBagService.findAllBagsByOrderId(any())).thenReturn(getBag1list());
            when(userNotificationRepository.save(any())).thenReturn(notification);
            when(notificationParameterRepository.saveAll(any())).thenReturn(new ArrayList<>(parameters));

            notificationService.notifyAllHalfPaidPackages();

            verify(userNotificationRepository, times(3)).save(notification);
            verify(notificationParameterRepository, times(3)).saveAll(any());
        }
    }

    @Test
    void testGetAllShortNotificationForUser() {
        String email = "email";
        String uuid = "uuid";

        when(userRemoteClient.findUuidByEmail(email)).thenReturn(uuid);
        when(userRepository.findByUuid(uuid)).thenReturn(TEST_USER);
        when(userNotificationRepository.findAllByUserAndIsDeletedFalse(TEST_USER, TEST_PAGEABLE))
            .thenReturn(TEST_PAGE);
        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
            NotificationType.UNPAID_ORDER,
            SITE)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));

        PageableAdvancedDto<NotificationShortDto> actual = notificationService
            .getAllShortNotificationsForUser(email, "uk", TEST_PAGEABLE);

        assertEquals(TEST_PAGEABLE_ADVANCED_DTO, actual);
    }

    @Test
    void testGetAllNotificationsForUser() {
        String userUuid = "user uuid";
        String language = "uk";
        Long orderId = 5L;
        Long notificationId = 0L;
        NotificationType notificationType = NotificationType.VIOLATION_THE_RULES;
        UserNotification userNotification = Mockito.mock(UserNotification.class);
        User user = Mockito.mock(User.class);
        Order order = Mockito.mock(Order.class);
        Page<UserNotification> page = new PageImpl<>(
            List.of(userNotification),
            Mockito.mock(Pageable.class),
            1L);
        Violation violation = Mockito.mock(Violation.class);
        List<String> images = List.of("image1", "image2", "image3");
        String notificationParameterValue = "value";
        Set<NotificationParameter> notificationParameters = Set.of(
            NotificationParameter.builder().key(VIOLATION_DESCRIPTION).value(notificationParameterValue).build());

        when(user.getUuid())
            .thenReturn(userUuid);
        when(userRepository.findByUuid(userUuid))
            .thenReturn(user);
        when(userNotification.getId())
            .thenReturn(notificationId);
        when(userNotificationRepository.findAllByUserAndIsDeletedFalse(user, TEST_PAGEABLE))
            .thenReturn(page);
        when(userNotificationRepository.findById(notificationId))
            .thenReturn(Optional.of(userNotification));
        when(userNotification.getUser())
            .thenReturn(user);
        when(userNotification.getNotificationType())
            .thenReturn(notificationType);
        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
            notificationType,
            SITE)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));
        when(userNotification.getParameters())
            .thenReturn(notificationParameters);
        when(userNotification.getOrder())
            .thenReturn(order);
        when(order.getId())
            .thenReturn(orderId);
        when(violationRepository.findByOrderIdAndDescription(orderId, notificationParameterValue))
            .thenReturn(Optional.of(violation));
        when(violation.getImages())
            .thenReturn(images);

        PageableAdvancedDto<NotificationFullDto> actual = notificationService
            .getAllNotificationsForUser(userUuid, language, TEST_PAGEABLE);

        assertEquals(TEST_NOTIFICATION_FULL_DTO_PAGEABLE, actual);
    }

    @Test
    void testGetAllNotificationForUserWhenNotificationDoesNotBelongToUser() {
        String userUuid = "user uuid";
        String anotherUserUuid = "another uuid";
        String language = "uk";
        Long notificationId = 0L;
        NotificationType notificationType = NotificationType.VIOLATION_THE_RULES;
        UserNotification userNotification = Mockito.mock(UserNotification.class);
        User user = Mockito.mock(User.class);
        User anotherUser = Mockito.mock(User.class);
        Page<UserNotification> page = new PageImpl<>(
            List.of(userNotification),
            Mockito.mock(Pageable.class),
            1L);

        when(userRepository.findByUuid(userUuid))
            .thenReturn(user);
        when(userNotificationRepository.findAllByUserAndIsDeletedFalse(user, TEST_PAGEABLE))
            .thenReturn(page);
        when(userNotificationRepository.findById(notificationId))
            .thenReturn(Optional.of(userNotification));
        when(userNotification.getNotificationType())
            .thenReturn(notificationType);
        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
            notificationType,
            SITE)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));
        when(userNotification.getUser())
            .thenReturn(anotherUser);
        when(anotherUser.getUuid())
            .thenReturn(anotherUserUuid);

        assertThrows(
            AccessDeniedException.class,
            () -> notificationService.getAllNotificationsForUser(
                userUuid,
                language,
                TEST_PAGEABLE));
    }

    @Test
    void testGetAllNotificationForUserWhenNotificationIsNotFound() {
        String userUuid = "user uuid";
        String language = "uk";
        Long notificationId = 1L;
        UserNotification userNotification = Mockito.mock(UserNotification.class);
        NotificationType notificationType = NotificationType.UNPAID_ORDER;
        User user = Mockito.mock(User.class);
        Page<UserNotification> page = new PageImpl<>(
            List.of(userNotification),
            Mockito.mock(Pageable.class),
            1L);
        Optional<UserNotification> notFoundNotification = Optional.empty();

        when(userRepository.findByUuid(userUuid))
            .thenReturn(user);
        when(userNotificationRepository.findAllByUserAndIsDeletedFalse(user, TEST_PAGEABLE))
            .thenReturn(page);
        when(userNotification.getNotificationType())
            .thenReturn(notificationType);
        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
            notificationType,
            SITE)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));
        when(userNotification.getId())
            .thenReturn(notificationId);
        when(userNotificationRepository.findById(notificationId))
            .thenReturn(notFoundNotification);

        assertThrows(
            NotFoundException.class,
            () -> notificationService.getAllNotificationsForUser(
                userUuid,
                language,
                TEST_PAGEABLE));
    }

    @Test
    void testGetUnreadNotifications() {
        assertEquals(0, notificationService.getUnreadNotifications("Test"));
    }

    @Test
    void testGetNotification() {
        when(userNotificationRepository.findById(1L)).thenReturn(Optional.of(TEST_USER_NOTIFICATION_4));
        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
            NotificationType.UNPAID_ORDER,
            SITE)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));

        NotificationDto actual = notificationService.getNotification("test", 1L, "uk");

        assertEquals(TEST_NOTIFICATION_DTO, actual);
    }

    @Test
    void testGetNotificationThrowsException() {
        when(userNotificationRepository.findById(1L)).thenReturn(Optional.of(TEST_USER_NOTIFICATION_4));

        assertThrows(AccessDeniedException.class,
            () -> notificationService.getNotification("testtest", 1L, "uk"));
    }

    @Test
    void getNotificationViolation() {
        UserNotification notification = createUserNotificationForViolationWithParameters();
        notification.getUser().setUuid("abc");
        when(userNotificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
            NotificationType.VIOLATION_THE_RULES,
            SITE)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));
        when(violationRepository.findByOrderIdAndDescription(notification.getOrder().getId(), "Description"))
            .thenReturn(Optional.of(getViolation()));

        NotificationDto actual = notificationService.getNotification("abc", 1L, "uk");

        assertEquals(createViolationNotificationDto(), actual);
    }

    @Test
    void testGetNotificationMarksAsReadWhenMarkAsReadTrue() {
        UserNotification notification = createUserNotificationForViolationWithParameters();
        notification.getUser().setUuid("abc");
        notification.setRead(false);

        when(userNotificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
            NotificationType.VIOLATION_THE_RULES, SITE)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));
        when(violationRepository.findByOrderIdAndDescription(notification.getOrder().getId(), "Description"))
            .thenReturn(Optional.of(getViolation()));
        when(userNotificationRepository.save(notification)).thenReturn(notification);

        NotificationDto actual = notificationService.getNotification("abc", 1L, "uk", true);

        assertEquals(createViolationNotificationDto(), actual);
        assertTrue(notification.isRead(), "Notification should be marked as read");
        verify(userNotificationRepository).save(notification);
    }

    @Test
    void testGetNotificationDoesNotMarkAsReadWhenMarkAsReadFalse() {
        UserNotification notification = createUserNotificationForViolationWithParameters();
        notification.getUser().setUuid("abc");
        notification.setRead(false);

        when(userNotificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
            NotificationType.VIOLATION_THE_RULES, SITE)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));
        when(violationRepository.findByOrderIdAndDescription(notification.getOrder().getId(), "Description"))
            .thenReturn(Optional.of(getViolation()));

        NotificationDto actual = notificationService.getNotification("abc", 1L, "uk", false);

        assertEquals(createViolationNotificationDto(), actual);
        assertFalse(notification.isRead(), "Notification should not be marked as read");
        verify(userNotificationRepository, never()).save(any());
    }

    @Test
    void testGetNotificationMarksAsReadThroughEndpoint() {
        UserNotification notification = createUserNotificationForViolationWithParameters();
        notification.getUser().setUuid("abc");
        notification.setRead(false);

        when(userNotificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
            NotificationType.VIOLATION_THE_RULES, SITE)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));
        when(violationRepository.findByOrderIdAndDescription(notification.getOrder().getId(), "Description"))
            .thenReturn(Optional.of(getViolation()));
        when(userNotificationRepository.save(notification)).thenReturn(notification);

        NotificationDto actual = notificationService.getNotification("abc", 1L, "uk");

        assertEquals(createViolationNotificationDto(), actual);
        assertTrue(notification.isRead(),
            "Notification should be marked as read when accessed through /notifications/{id}");
        verify(userNotificationRepository).save(notification);
    }

    @Test
    void testGetNotificationDoesNotSaveWhenAlreadyReadAndMarkAsReadTrue() {
        UserNotification notification = createUserNotificationForViolationWithParameters();
        notification.getUser().setUuid("abc");
        notification.setRead(true);

        when(userNotificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
            NotificationType.VIOLATION_THE_RULES, SITE)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));
        when(violationRepository.findByOrderIdAndDescription(notification.getOrder().getId(), "Description"))
            .thenReturn(Optional.of(getViolation()));

        NotificationDto actual = notificationService.getNotification("abc", 1L, "uk", true);

        assertEquals(createViolationNotificationDto(), actual);
        assertTrue(notification.isRead(), "Notification should remain read");
        verify(userNotificationRepository, never()).save(any());
    }

    @Test
    void testGetNotificationViolationNotFoundException() {
        UserNotification notification = createUserNotificationForViolationWithParameters();
        notification.getUser().setUuid("abc");
        when(userNotificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
            NotificationType.VIOLATION_THE_RULES,
            SITE)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));
        when(violationRepository.findByOrderIdAndDescription(notification.getOrder().getId(), "Description"))
            .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> notificationService.getNotification("abc", 1L, "uk"));
    }

    @Test
    void testViewNotification() {
        Long notificationId = TEST_NOTIFICATION_TEMPLATE.getId();
        Long userId = TEST_USER.getId();

        when(userRepository.findByUuid(any())).thenReturn(TEST_USER);
        when(userNotificationRepository.existsByIdAndUserIdAndIsDeletedFalse(notificationId, userId)).thenReturn(true);

        notificationService.viewNotification(notificationId, TEST_UUID);

        verify(userRepository).findByUuid(any());
        verify(userNotificationRepository).existsByIdAndUserIdAndIsDeletedFalse(notificationId, userId);
        verify(userNotificationRepository).markNotificationAsViewed(notificationId);
    }

    @Test
    void testReadNonExistentNotificationAndGetNotFoundException() {
        Long notificationId = TEST_NOTIFICATION_TEMPLATE.getId();
        Long userId = TEST_USER.getId();

        when(userRepository.findByUuid(TEST_UUID)).thenReturn(TEST_USER);
        when(userNotificationRepository.existsByIdAndUserIdAndIsDeletedFalse(notificationId, userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> notificationService.viewNotification(notificationId, TEST_UUID));

        assertEquals(ErrorMessage.NOTIFICATION_DOES_NOT_EXIST, exception.getMessage());

        verify(userRepository).findByUuid(TEST_UUID);
        verify(userNotificationRepository).existsByIdAndUserIdAndIsDeletedFalse(notificationId, userId);
        verify(userNotificationRepository, never()).markNotificationAsViewed(notificationId);

    }

    @Test
    void testUnreadNotification() {
        Long notificationId = TEST_NOTIFICATION_TEMPLATE.getId();
        Long userId = TEST_USER.getId();

        when(userRepository.findByUuid(any())).thenReturn(TEST_USER);
        when(userNotificationRepository.existsByIdAndUserIdAndIsDeletedFalse(notificationId, userId)).thenReturn(true);

        notificationService.unreadNotification(notificationId, TEST_UUID);

        verify(userRepository).findByUuid(any());
        verify(userNotificationRepository).existsByIdAndUserIdAndIsDeletedFalse(notificationId, userId);
        verify(userNotificationRepository).markNotificationAsNotViewed(notificationId);
    }

    @Test
    void testUnreadNonExistentNotificationAndGetNotFoundException() {
        Long notificationId = TEST_NOTIFICATION_TEMPLATE.getId();
        Long userId = TEST_USER.getId();

        when(userRepository.findByUuid(TEST_UUID)).thenReturn(TEST_USER);
        when(userNotificationRepository.existsByIdAndUserIdAndIsDeletedFalse(notificationId, userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> notificationService.unreadNotification(notificationId, TEST_UUID));

        assertEquals(ErrorMessage.NOTIFICATION_DOES_NOT_EXIST, exception.getMessage());

        verify(userRepository).findByUuid(TEST_UUID);
        verify(userNotificationRepository).existsByIdAndUserIdAndIsDeletedFalse(notificationId, userId);
        verify(userNotificationRepository, never()).markNotificationAsNotViewed(notificationId);

    }

    @Test
    void testDeleteNotification() {
        Long notificationId = TEST_NOTIFICATION_TEMPLATE.getId();
        Long userId = TEST_USER.getId();

        when(userRepository.findByUuid(any())).thenReturn(TEST_USER);
        when(userNotificationRepository.existsByIdAndUserIdAndIsDeletedFalse(notificationId, userId)).thenReturn(true);

        notificationService.deleteNotification(notificationId, TEST_UUID);

        verify(userRepository).findByUuid(any());
        verify(userNotificationRepository).existsByIdAndUserIdAndIsDeletedFalse(notificationId, userId);
        verify(userNotificationRepository).markAsDeletedUserNotificationByIdAndUserId(notificationId, userId);
    }

    @Test
    void testDeleteNonExistentNotificationAndGetNotFoundException() {
        Long notificationId = TEST_NOTIFICATION_TEMPLATE.getId();
        Long userId = TEST_USER.getId();

        when(userRepository.findByUuid(TEST_UUID)).thenReturn(TEST_USER);
        when(userNotificationRepository.existsByIdAndUserIdAndIsDeletedFalse(notificationId, userId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> notificationService.deleteNotification(notificationId, TEST_UUID));

        assertEquals(ErrorMessage.NOTIFICATION_DOES_NOT_EXIST, exception.getMessage());

        verify(userRepository).findByUuid(TEST_UUID);
        verify(userNotificationRepository).existsByIdAndUserIdAndIsDeletedFalse(notificationId, userId);
        verify(userNotificationRepository, never()).markAsDeletedUserNotificationByIdAndUserId(notificationId, userId);

    }

    @Test
    void testNotifyUnpaidOrderForBroughtByHimself() {
        User user = getUser();
        Order order = ModelUtils.getOrdersStatusBROUGHT_IT_HIMSELFDto();
        order.setConfirmedQuantity(Collections.singletonMap(1, 1));
        order.setExportedQuantity(Collections.emptyMap());
        order.setEvents(List.of(Event.builder().eventNameUk(ORDER_FORMED_UK).build()));
        order.setPayment(TEST_PAYMENT_LIST);
        order.setPointsToUse(0);
        order.setCertificates(Collections.emptySet());
        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.ORDER_STATUS_CHANGED);
        notification.setUser(user);
        notification.setOrder(order);

        when(userNotificationRepository.save(any())).thenReturn(notification);
        when(notificationParameterRepository.saveAll(any())).thenReturn(new ArrayList<>(new HashSet<>()));
        when(orderBagService.findAllBagsByOrderId(any())).thenReturn(getBag4list());
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        notificationService.notifyUnpaidOrder(order.getId(), PAYMENT_LINK);

        verify(userNotificationRepository).save(any());
        verify(notificationParameterRepository).saveAll(any());
    }

    @Test
    void testNotifyUnpaidOrderForDone() {
        User user = getUser();
        Order order = ModelUtils.getOrdersStatusDoneDto();
        order.setConfirmedQuantity(Collections.singletonMap(1, 1));
        order.setExportedQuantity(Collections.singletonMap(1, 1));
        Event formed = Event.builder().eventNameUk(ORDER_FORMED_UK).build();
        Event adjustment = Event.builder().eventNameUk(ORDER_ADJUSTMENT_UK).build();
        Event confirmed = Event.builder().eventNameUk(ORDER_CONFIRMED_UK).build();
        Event onTheRoad = Event.builder().eventNameUk(ORDER_ON_THE_ROUTE_UK).build();
        order.setEvents(List.of(formed, adjustment, confirmed, onTheRoad));
        order.setPayment(TEST_PAYMENT_LIST);
        order.setPointsToUse(0);
        order.setCertificates(Collections.emptySet());

        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.DONE_OR_CANCELED_UNPAID_ORDER);
        notification.setUser(user);
        notification.setOrder(order);

        when(userNotificationRepository.save(any())).thenReturn(notification);
        when(notificationParameterRepository.saveAll(any())).thenReturn(new ArrayList<>(new HashSet<>()));
        when(orderBagService.findAllBagsByOrderId(any())).thenReturn(getBag4list());
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        notificationService.notifyUnpaidOrder(order.getId(), PAYMENT_LINK);

        verify(userNotificationRepository).save(any());
        verify(notificationParameterRepository).saveAll(any());
    }

    @Test
    void testNotifyUnpaidOrderForCancel() {
        User user = getUser();
        Order order = ModelUtils.getCanceledPaidOrder();
        order.setConfirmedQuantity(Collections.emptyMap());
        order.setExportedQuantity(Collections.emptyMap());
        order.setAmountOfBagsOrdered(Collections.singletonMap(1, 1));
        Event formed = Event.builder().eventNameUk(ORDER_FORMED_UK).build();
        Event adjustment = Event.builder().eventNameUk(ORDER_ADJUSTMENT_UK).build();
        Event confirmed = Event.builder().eventNameUk(ORDER_CONFIRMED_UK).build();
        Event onTheRoad = Event.builder().eventNameUk(ORDER_ON_THE_ROUTE_UK).build();
        order.setEvents(List.of(formed, adjustment, confirmed, onTheRoad));
        order.setPayment(TEST_PAYMENT_LIST);
        order.setPointsToUse(0);
        order.setCertificates(Collections.emptySet());

        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.DONE_OR_CANCELED_UNPAID_ORDER);
        notification.setUser(user);
        notification.setOrder(order);

        when(userNotificationRepository.save(any())).thenReturn(notification);
        when(notificationParameterRepository.saveAll(any())).thenReturn(new ArrayList<>(new HashSet<>()));
        when(orderBagService.findAllBagsByOrderId(any())).thenReturn(getBag4list());
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        notificationService.notifyUnpaidOrder(order.getId(), PAYMENT_LINK);

        verify(userNotificationRepository).save(any());
        verify(notificationParameterRepository).saveAll(any());
    }

    @Test
    void testNotifyHalfPaidOrderForDone() {
        User user = getUser();
        Order order = ModelUtils.getOrdersStatusDoneDto();
        order.setConfirmedQuantity(Collections.singletonMap(1, 1));
        order.setExportedQuantity(Collections.singletonMap(1, 1));
        Event formed = Event.builder().eventNameUk(ORDER_FORMED_UK).build();
        Event adjustment = Event.builder().eventNameUk(ORDER_ADJUSTMENT_UK).build();
        Event confirmed = Event.builder().eventNameUk(ORDER_CONFIRMED_UK).build();
        Event onTheRoad = Event.builder().eventNameUk(ORDER_ON_THE_ROUTE_UK).build();
        order.setEvents(List.of(formed, adjustment, confirmed, onTheRoad));
        order.setPayment(TEST_PAYMENT_LIST);
        order.setPointsToUse(0);
        order.setCertificates(Collections.emptySet());

        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.DONE_OR_CANCELED_UNPAID_ORDER);
        notification.setUser(user);
        notification.setOrder(order);

        when(userNotificationRepository.save(any())).thenReturn(notification);
        when(notificationParameterRepository.saveAll(any())).thenReturn(new ArrayList<>(new HashSet<>()));
        when(orderBagService.findAllBagsByOrderId(any())).thenReturn(getBag4list());
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        notificationService.notifyHalfPaidPackage(order.getId());

        verify(userNotificationRepository).save(any());
        verify(notificationParameterRepository).saveAll(any());
    }

    @Test
    void testNotifyHalfPaidOrderForBroughtByHimself() {
        User user = getUser();
        Order order = ModelUtils.getOrdersStatusBROUGHT_IT_HIMSELFDto();
        order.setConfirmedQuantity(Collections.singletonMap(1, 1));
        order.setExportedQuantity(Collections.emptyMap());
        order.setEvents(List.of(Event.builder().eventNameUk(ORDER_FORMED_UK).build()));
        order.setPayment(TEST_PAYMENT_LIST);
        order.setPointsToUse(0);
        order.setCertificates(Collections.emptySet());

        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.ORDER_STATUS_CHANGED);
        notification.setUser(user);
        notification.setOrder(order);

        when(userNotificationRepository.save(any())).thenReturn(notification);
        when(notificationParameterRepository.saveAll(any())).thenReturn(new ArrayList<>(new HashSet<>()));
        when(orderBagService.findAllBagsByOrderId(any())).thenReturn(getBag4list());
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        notificationService.notifyHalfPaidPackage(order.getId());

        verify(userNotificationRepository).save(any());
        verify(notificationParameterRepository).saveAll(any());
    }

    @Test
    void testCreateNotificationDtoTitleUaLanguage() {
        String language = "uk";

        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(any(), any()))
            .thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));

        NotificationDto result = NotificationServiceImpl.createNotificationDto(TEST_USER_NOTIFICATION, language,
            NotificationReceiverType.MOBILE, templateRepository, 5L);

        assertEquals(TEST_NOTIFICATION_TEMPLATE.getTitleUk(), result.getTitle());
    }

    @Test
    void testCreateNotificationDtoTitleEnLanguage() {
        String language = "en";

        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(any(), any()))
            .thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));

        NotificationDto result = NotificationServiceImpl.createNotificationDto(TEST_USER_NOTIFICATION, language,
            NotificationReceiverType.MOBILE, templateRepository, 5L);

        assertEquals(TEST_NOTIFICATION_TEMPLATE.getTitleEn(), result.getTitle());
    }

    @Test
    void testCreateNotificationDtoOfCustomTemplate() {
        String language = "en";

        var testNotificationTemplate = ModelUtils.createNotificationTemplate();
        testNotificationTemplate.setNotificationType(NotificationType.CUSTOM);
        testNotificationTemplate.setTrigger(NotificationTrigger.CUSTOM);

        var testUserNotification = UserNotification.builder()
            .notificationType(NotificationType.CUSTOM)
            .user(TEST_USER_NOTIFICATION.getUser())
            .order(TEST_USER_NOTIFICATION.getOrder())
            .templateId(testNotificationTemplate.getId())
            .build();

        when(templateRepository.findNotificationTemplateByIdAndNotificationReceiverType(any(), any()))
            .thenReturn(Optional.of(testNotificationTemplate));

        NotificationDto result = NotificationServiceImpl.createNotificationDto(testUserNotification, language,
            NotificationReceiverType.MOBILE, templateRepository, 5L);

        assertEquals(testNotificationTemplate.getTitleEn(), result.getTitle());
        verify(templateRepository).findNotificationTemplateByIdAndNotificationReceiverType(any(), any());
    }

    @Test
    void testNotifySelfPickupOrder() {
        User user = getUser();
        Long orderId = 2L;
        Order order = Order.builder()
            .id(orderId)
            .user(user)
            .build();
        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.ORDER_STATUS_CHANGED);
        notification.setUser(user);
        notification.setOrder(order);
        Set<NotificationParameter> parameters = Set.of(NotificationParameter.builder()
            .key("orderNumber")
            .value(orderId.toString())
            .userNotification(notification)
            .build());

        when(userNotificationRepository.save(any())).thenReturn(notification);
        when(notificationParameterRepository.saveAll(any())).thenReturn(new ArrayList<>(parameters));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        notificationService.notifySelfPickupOrder(order.getId());

        verify(userNotificationRepository).save(notification);
        verify(notificationParameterRepository).saveAll(parameters);
    }

    @Test
    void testNotifyAllHalfPaidPackagesWhenHalfPaidPackageDoesNotNeedNotification() {
        Order order = Order.builder()
            .id(1L)
            .orderPaymentStatus(OrderPaymentStatus.HALF_PAID)
            .orderDate(LocalDateTime.now(fixedClock).minusMonths(2))
            .build();

        when(orderRepository.findAllByOrderPaymentStatus(OrderPaymentStatus.HALF_PAID))
            .thenReturn(List.of(order));
        when(userNotificationRepository.findFirstByOrderIdAndNotificationTypeInOrderByNotificationTimeDesc(
            eq(order.getId()), any(NotificationType.class), any(NotificationType.class), any(NotificationType.class)))
            .thenReturn(Optional.empty());

        notificationService.notifyAllHalfPaidPackages();

        verify(userNotificationRepository, never()).save(any());
        verify(notificationParameterRepository, never()).saveAll(any());
    }

    @Test
    void testNotifyCreateNewOrder() {
        User user = getUser();
        Order newOrder = ModelUtils.getOrder();
        newOrder.setSumTotalAmountWithoutDiscounts(100L);
        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.CREATE_NEW_ORDER);
        notification.setUser(user);
        notification.setOrder(newOrder);
        Set<NotificationParameter> parameters = Set.of(NotificationParameter.builder()
            .key(ORDER_NUMBER_KEY)
            .value(newOrder.getId().toString())
            .build(),
            NotificationParameter.builder()
                .key(AMOUNT_TO_PAY_KEY)
                .value(String.valueOf(newOrder.getSumTotalAmountWithoutDiscounts() / 100))
                .build(),
            NotificationParameter.builder()
                .key(CUSTOMER)
                .value(newOrder.getUser().getRecipientName())
                .build());

        when(userNotificationRepository.save(any())).thenReturn(notification);
        when(notificationParameterRepository.saveAll(any())).thenReturn(new ArrayList<>(parameters));
        when(orderRepository.findById(newOrder.getId())).thenReturn(Optional.of(newOrder));

        notificationService.notifyCreatedOrder(newOrder.getId());

        verify(userNotificationRepository).save(notification);
        verify(notificationParameterRepository).saveAll(parameters);
    }

    @Test
    void testNotifyManagerWithNewGreenOfficeRequestFromTelegramBot() {
        ScheduledEmailMessage notification = ScheduledEmailMessage
            .builder()
            .username(USERNAME)
            .subject(telegramBotResponseService.getResponseByLangAndMessageType(TelegramBotConstants.UK,
                MessageType.GREEN_OFFICE_SUBJECT))
            .body(USER_EMAIL)
            .language(AppConstant.LOCALE_UK_NAME)
            .isUbs(true)
            .build();
        doNothing().when(userRemoteClient).sendGreenOfficeRequestNotification(notification);

        notificationService.notifyManagerWithNewGreenOfficeRequestFromTelegramBot(USER_EMAIL, USERNAME,
            TelegramBotConstants.UK);

        verify(userRemoteClient, times(1)).sendGreenOfficeRequestNotification(notification);
    }

    @Test
    void testNotifyCanceledOrder() {
        User user = getUser();
        Order order = Order.builder()
            .id(1L)
            .user(user)
            .sumTotalAmountWithoutDiscounts(10000L)
            .build();
        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.CANCELED_ORDER);
        notification.setUser(user);
        notification.setOrder(order);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        when(userNotificationRepository.save(any())).thenReturn(notification);
        when(notificationParameterRepository.saveAll(any())).thenReturn(new ArrayList<>(new HashSet<>()));

        notificationService.notifyCanceledOrder(order.getId());

        verify(userNotificationRepository).save(any());
        verify(notificationParameterRepository).saveAll(any());
    }

    @Test
    void testGetAllNotificationsForUserForCustomNotifications() {
        User user = TEST_USER;
        String language = "en";
        UserNotification customNotification1 = UserNotification.builder()
            .id(1L)
            .user(user)
            .notificationType(NotificationType.CUSTOM)
            .templateId(1L)
            .build();
        UserNotification customNotification2 = UserNotification.builder()
            .id(2L)
            .user(user)
            .notificationType(NotificationType.CUSTOM)
            .templateId(1L)
            .build();

        Page<UserNotification> page = new PageImpl<>(List.of(customNotification1, customNotification2),
            Mockito.mock(Pageable.class),
            2L);

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);

        when(templateRepository.findNotificationTemplateByIdAndNotificationReceiverType(1L, SITE))
            .thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE_2));

        when(userNotificationRepository.findAllByUserAndIsDeletedFalse(user, TEST_PAGEABLE)).thenReturn(page);

        when(userNotificationRepository.findById(1L)).thenReturn(Optional.of(customNotification1));
        when(userNotificationRepository.findById(2L)).thenReturn(Optional.of(customNotification2));

        PageableAdvancedDto<NotificationFullDto> actual =
            notificationService.getAllNotificationsForUser(user.getUuid(), language, TEST_PAGEABLE);

        assertEquals(TEST_NOTIFICATION_FULL_DTO_PAGEABLE_2, actual);
    }
}