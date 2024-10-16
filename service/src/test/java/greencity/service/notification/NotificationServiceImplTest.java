package greencity.service.notification;

import com.google.common.util.concurrent.MoreExecutors;
import greencity.ModelUtils;
import greencity.config.InternalUrlConfigProp;
import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.NotificationShortDto;
import greencity.dto.pageble.PageableDto;
import greencity.entity.order.Event;
import greencity.enums.NotificationTrigger;
import greencity.enums.NotificationType;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.enums.NotificationReceiverType;
import greencity.entity.notifications.NotificationParameter;
import greencity.entity.notifications.UserNotification;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.enums.UserCategory;
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
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static greencity.ModelUtils.TEST_DTO;
import static greencity.ModelUtils.TEST_NOTIFICATION_DTO;
import static greencity.ModelUtils.TEST_NOTIFICATION_PARAMETER_SET;
import static greencity.ModelUtils.TEST_NOTIFICATION_PARAMETER_SET2;
import static greencity.ModelUtils.TEST_NOTIFICATION_TEMPLATE;
import static greencity.ModelUtils.TEST_ORDER_2;
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
import static greencity.ModelUtils.createUserNotificationForViolationWithParameters;
import static greencity.ModelUtils.createViolationNotificationDto;
import static greencity.ModelUtils.getBag1list;
import static greencity.ModelUtils.getBag4list;
import static greencity.ModelUtils.getActiveCertificateWith10Points;
import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.getViolation;
import static greencity.ModelUtils.getNotifyInternallyFormedOrder;
import static greencity.enums.NotificationReceiverType.SITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.anyLong;
import static greencity.constant.OrderHistory.ADD_VIOLATION;
import static greencity.constant.OrderHistory.CHANGES_VIOLATION;
import static greencity.constant.OrderHistory.DELETE_VIOLATION;
import static greencity.constant.OrderHistory.ORDER_ADJUSTMENT;
import static greencity.constant.OrderHistory.ORDER_CONFIRMED;
import static greencity.constant.OrderHistory.ORDER_FORMED;
import static greencity.constant.OrderHistory.ORDER_NOT_TAKEN_OUT;
import static greencity.constant.OrderHistory.ORDER_ON_THE_ROUTE;
import static java.util.Arrays.asList;

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
    private NotificationTemplateRepository templateRepository;

    @Mock
    private Clock clock;

    @Mock
    private ExecutorService executorService;

    @Spy
    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private InternalUrlConfigProp internalUrlConfigProp;

    private Clock fixedClock;

    ExecutorService mockExecutor = MoreExecutors.newDirectExecutorService();
    @Mock
    private OrderBagService orderBagService;

    @Nested
    class ClockNotification {
        @BeforeEach
        public void setUp() {
            fixedClock = Clock.fixed(LOCAL_DATE_TIME.toInstant(ZoneOffset.ofHours(0)), ZoneId.systemDefault());
            lenient().doReturn(fixedClock.instant()).when(clock).instant();
            lenient().doReturn(fixedClock.getZone()).when(clock).getZone();
        }

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

            when(orderRepository.findAllByOrderPaymentStatus(OrderPaymentStatus.UNPAID))
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

            when(userNotificationRepository.save(any())).thenReturn(created);

            List<NotificationParameter> notificationParameters = List.of(
                NotificationParameter.builder().id(1L)
                    .userNotification(created).key("orderNumber")
                    .value(orders.getFirst().getId().toString()).build(),
                NotificationParameter.builder().id(2L)
                    .userNotification(created).key("amountToPay")
                    .value("10000").build());

            when(notificationParameterRepository.saveAll(any())).thenReturn(notificationParameters);

            notificationService.notifyUnpaidOrders();

            verify(notificationParameterRepository, times(3)).saveAll(any());
            verify(userNotificationRepository, times(3)).save(any());
        }

        @Test
        void testNotifyPaidOrder() {
            when(userNotificationRepository.save(any())).thenReturn(TEST_USER_NOTIFICATION);

            notificationService.notifyPaidOrder(TEST_ORDER_2);

            verify(userNotificationRepository, times(1)).save(any());

        }

        @Test
        @SneakyThrows
        void notifyPaidOrder() {
            Order order = Order.builder().id(1L).build();
            NotificationParameter orderNumber = NotificationParameter.builder()
                .key("orderNumber")
                .value(order.getId().toString())
                .build();
            when(notificationParameterRepository.saveAll(Set.of(orderNumber))).thenReturn(List.of(orderNumber));
            when(userNotificationRepository.save(any())).thenReturn(TEST_USER_NOTIFICATION);

            notificationService.notifyPaidOrder(order);

            verify(notificationService).notifyPaidOrder(order);
            verify(userNotificationRepository).save(any(UserNotification.class));
            verify(notificationParameterRepository).saveAll(Set.of(orderNumber));
        }

        @Test
        void testNotifyCourierItineraryFormed() {
            Order order = getNotifyInternallyFormedOrder();

            UserNotification userNotification = getInternallyFormedOrderUserNotification(order);

            List<NotificationParameter> parameters = courierInternallyFormedParameters(order);
            when(userNotificationRepository.save(any())).thenReturn(userNotification);

            parameters.forEach(parameter -> parameter.setUserNotification(userNotification));
            when(notificationParameterRepository.saveAll(new HashSet<>(parameters))).thenReturn(parameters);

            notificationService.notifyCourierItineraryFormed(order);

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
        void TestNotifyChangedViolation() {
            Set<NotificationParameter> parameters = new HashSet<>();
            parameters.add(NotificationParameter.builder()
                .key("orderNumber")
                .value("46")
                .build());
            Violation violation = TEST_VIOLATION.setOrder(TEST_ORDER_4);
            when(userNotificationRepository.save(TEST_USER_NOTIFICATION_6)).thenReturn(TEST_USER_NOTIFICATION_6);
            parameters.forEach(p -> p.setUserNotification(TEST_USER_NOTIFICATION_6));
            when(notificationParameterRepository.saveAll(parameters)).thenReturn(new LinkedList<>(parameters));

            notificationService.notifyChangedViolation(violation, TEST_ORDER_4.getId());

            verify(userNotificationRepository).save(any());
            verify(notificationParameterRepository).saveAll(parameters);
        }

        @Test
        void TestNotifyDeleteViolation() {
            Set<NotificationParameter> parameters = new HashSet<>();
            parameters.add(NotificationParameter.builder()
                .key("orderNumber")
                .value("46")
                .build());
            Violation violation = TEST_VIOLATION.setOrder(TEST_ORDER_4);
            when(violationRepository.findActiveViolationByOrderId(TEST_ORDER_4.getId()))
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
            setEventsToOrder(order, ADD_VIOLATION);
            Violation violation = getOrdersViolations(order);
            Set<NotificationParameter> parameters = getNewViolationParameter(violation);

            mockUserNeedNotificationCheck(order, NotificationType.VIOLATION_THE_RULES);
            when(orderRepository.findAllWithEventsByEventNames(ADD_VIOLATION, CHANGES_VIOLATION, DELETE_VIOLATION))
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
            setEventsToOrder(order, ADD_VIOLATION, CHANGES_VIOLATION);

            when(orderRepository.findAllWithEventsByEventNames(ADD_VIOLATION, CHANGES_VIOLATION, DELETE_VIOLATION))
                .thenReturn(orders);

            notificationService.notifyAllAddedViolations();

            verify(orderRepository).findAllWithEventsByEventNames(anyString(), anyString(), anyString());
        }

        @Test
        void testNotifyAllChangedViolations() {
            Order order = TEST_ORDER_4;
            List<Order> orders = Collections.singletonList(order);
            setEventsToOrder(order, CHANGES_VIOLATION);
            Set<NotificationParameter> parameters = getViolationParameter(order);

            mockUserNeedNotificationCheck(order, NotificationType.CHANGED_IN_RULE_VIOLATION_STATUS);
            when(orderRepository.findAllWithEventsByEventNames(CHANGES_VIOLATION, DELETE_VIOLATION))
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
            setEventsToOrder(order, CHANGES_VIOLATION, DELETE_VIOLATION);

            when(orderRepository.findAllWithEventsByEventNames(CHANGES_VIOLATION, DELETE_VIOLATION))
                .thenReturn(orders);

            notificationService.notifyAllChangedViolations();

            verify(orderRepository).findAllWithEventsByEventNames(anyString(), anyString());
        }

        @Test
        void testNotifyAllCanceledViolations() {
            Order order = TEST_ORDER_4;
            List<Order> orders = Collections.singletonList(order);
            setEventsToOrder(order, DELETE_VIOLATION);
            Set<NotificationParameter> parameters = getViolationParameter(order);

            mockUserNeedNotificationCheck(order, NotificationType.CANCELED_VIOLATION_THE_RULES_BY_THE_MANAGER);
            when(orderRepository.findAllWithEventsByEventNames(DELETE_VIOLATION)).thenReturn(orders);
            mockFillAndSendNotification(parameters, order,
                NotificationType.CANCELED_VIOLATION_THE_RULES_BY_THE_MANAGER);

            notificationService.notifyAllCanceledViolations();

            verifyUserNeedNotificationCheck();
            verify(orderRepository).findAllWithEventsByEventNames(anyString());
            verifyFillAndSendNotification();
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
            setEventsToOrder(order, ORDER_ADJUSTMENT, ORDER_CONFIRMED, ORDER_ON_THE_ROUTE);
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
            setEventsToOrder(order, ORDER_NOT_TAKEN_OUT, ADD_VIOLATION);
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
            setEventsToOrder(order, ORDER_ADJUSTMENT, ORDER_CONFIRMED);
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
                Arguments.of(OrderStatus.CONFIRMED, asList(ORDER_CONFIRMED, ORDER_ON_THE_ROUTE, ORDER_NOT_TAKEN_OUT)),
                Arguments.of(OrderStatus.DONE, Collections.singletonList(ORDER_CONFIRMED)),
                Arguments.of(OrderStatus.CANCELED, asList(ORDER_CONFIRMED, ORDER_ON_THE_ROUTE)));
        }

        @ParameterizedTest
        @MethodSource("wrongForUnpaidOrderStatusesProvider")
        void testNotifyUnpaidPackagesWhenOrderHasUnacceptableStatusesAndEvents(OrderStatus status) {
            Order order = getOrderWithAmountToPay();
            order.setOrderStatus(status);
            setEventsToOrder(order, ORDER_CONFIRMED, ORDER_ON_THE_ROUTE, ORDER_NOT_TAKEN_OUT);
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
                .map(e -> Event.builder().eventName(e).build())
                .toList();
            order.setEvents(events);
        }

        private void setEventsToOrder(Order order, List<String> eventNames) {
            List<Event> events = eventNames.stream()
                .map(e -> Event.builder().eventName(e).build())
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

        private void mockUserNeedNotificationCheck(Order order,NotificationType notificationType){
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
        void testNotifyInactiveAccounts() {
            AbstractNotificationProvider abstractNotificationProvider =
                Mockito.mock(AbstractNotificationProvider.class);
            NotificationServiceImpl notificationService1 = new NotificationServiceImpl(
                userRepository,
                userNotificationRepository,
                orderRepository,
                violationRepository,
                notificationParameterRepository,
                clock,
                List.of(abstractNotificationProvider),
                templateRepository,
                mockExecutor,
                internalUrlConfigProp, orderBagService);
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
                .value(orders.get(0).getId().toString()).build());

            when(orderBagService.findAllBagsByOrderId(any())).thenReturn(getBag1list());
            when(userNotificationRepository.save(any())).thenReturn(notification);
            when(notificationParameterRepository.saveAll(any())).thenReturn(new ArrayList<>(parameters));

            notificationService.notifyAllHalfPaidPackages();

            verify(userNotificationRepository, times(3)).save(notification);
            verify(notificationParameterRepository, times(3)).saveAll(any());
        }
    }

    @Test
    void testGetAllNotificationForUser() {
        when(userRepository.findByUuid("Test")).thenReturn(TEST_USER);
        when(userNotificationRepository.findAllByUser(TEST_USER, TEST_PAGEABLE))
            .thenReturn(TEST_PAGE);
        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
            NotificationType.UNPAID_ORDER,
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
        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
            NotificationType.UNPAID_ORDER,
            SITE)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));

        NotificationDto actual = notificationService.getNotification("test", 1L, "ua");

        assertEquals(TEST_NOTIFICATION_DTO, actual);
    }

    @Test
    void testGetNotificationThrowsException() {
        when(userNotificationRepository.findById(1L)).thenReturn(Optional.of(TEST_USER_NOTIFICATION_4));

        assertThrows(AccessDeniedException.class,
            () -> notificationService.getNotification("testtest", 1L, "ua"));
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

        NotificationDto actual = notificationService.getNotification("abc", 1L, "ua");

        assertEquals(createViolationNotificationDto(), actual);
    }

    @Test
    void getNotificationViolationNotFoundException() {
        UserNotification notification = createUserNotificationForViolationWithParameters();
        notification.getUser().setUuid("abc");
        when(userNotificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
            NotificationType.VIOLATION_THE_RULES,
            SITE)).thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));
        when(violationRepository.findByOrderIdAndDescription(notification.getOrder().getId(), "Description"))
            .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> notificationService.getNotification("abc", 1L, "ua"));
    }

    @Test
    void testNotifyUnpaidOrderForBroughtByHimself() {
        User user = getUser();
        Order order = ModelUtils.getOrdersStatusBROUGHT_IT_HIMSELFDto();
        order.setConfirmedQuantity(Collections.singletonMap(1, 1));
        order.setExportedQuantity(Collections.emptyMap());
        order.setEvents(List.of(Event.builder().eventName(ORDER_FORMED).build()));
        order.setPayment(TEST_PAYMENT_LIST);
        order.setPointsToUse(0);
        order.setCertificates(Collections.emptySet());
        Set<NotificationParameter> parameters = new HashSet<>();

        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.ORDER_STATUS_CHANGED);
        notification.setUser(user);
        notification.setOrder(order);

        when(userNotificationRepository.save(any())).thenReturn(notification);
        when(notificationParameterRepository.saveAll(any())).thenReturn(new ArrayList<>(parameters));
        when(orderBagService.findAllBagsByOrderId(any())).thenReturn(getBag4list());

        notificationService.notifyUnpaidOrder(order);

        verify(userNotificationRepository).save(any());
        verify(notificationParameterRepository).saveAll(any());
    }

    @Test
    void testNotifyUnpaidOrderForDone() {
        User user = getUser();
        Order order = ModelUtils.getOrdersStatusDoneDto();
        order.setConfirmedQuantity(Collections.singletonMap(1, 1));
        order.setExportedQuantity(Collections.singletonMap(1, 1));
        Event formed = Event.builder().eventName(ORDER_FORMED).build();
        Event adjustment = Event.builder().eventName(ORDER_ADJUSTMENT).build();
        Event confirmed = Event.builder().eventName(ORDER_CONFIRMED).build();
        Event onTheRoad = Event.builder().eventName(ORDER_ON_THE_ROUTE).build();
        order.setEvents(List.of(formed, adjustment, confirmed, onTheRoad));
        order.setPayment(TEST_PAYMENT_LIST);
        order.setPointsToUse(0);
        order.setCertificates(Collections.emptySet());
        Set<NotificationParameter> parameters = new HashSet<>();

        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.DONE_OR_CANCELED_UNPAID_ORDER);
        notification.setUser(user);
        notification.setOrder(order);

        when(userNotificationRepository.save(any())).thenReturn(notification);
        when(notificationParameterRepository.saveAll(any())).thenReturn(new ArrayList<>(parameters));
        when(orderBagService.findAllBagsByOrderId(any())).thenReturn(getBag4list());
        notificationService.notifyUnpaidOrder(order);

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
        Event formed = Event.builder().eventName(ORDER_FORMED).build();
        Event adjustment = Event.builder().eventName(ORDER_ADJUSTMENT).build();
        Event confirmed = Event.builder().eventName(ORDER_CONFIRMED).build();
        Event onTheRoad = Event.builder().eventName(ORDER_ON_THE_ROUTE).build();
        order.setEvents(List.of(formed, adjustment, confirmed, onTheRoad));
        order.setPayment(TEST_PAYMENT_LIST);
        order.setPointsToUse(0);
        order.setCertificates(Collections.emptySet());
        Set<NotificationParameter> parameters = new HashSet<>();

        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.DONE_OR_CANCELED_UNPAID_ORDER);
        notification.setUser(user);
        notification.setOrder(order);

        when(userNotificationRepository.save(any())).thenReturn(notification);
        when(notificationParameterRepository.saveAll(any())).thenReturn(new ArrayList<>(parameters));
        when(orderBagService.findAllBagsByOrderId(any())).thenReturn(getBag4list());

        notificationService.notifyUnpaidOrder(order);

        verify(userNotificationRepository).save(any());
        verify(notificationParameterRepository).saveAll(any());
    }

    @Test
    void testNotifyHalfPaidOrderForDone() {
        User user = getUser();
        Order order = ModelUtils.getOrdersStatusDoneDto();
        order.setConfirmedQuantity(Collections.singletonMap(1, 1));
        order.setExportedQuantity(Collections.singletonMap(1, 1));
        Event formed = Event.builder().eventName(ORDER_FORMED).build();
        Event adjustment = Event.builder().eventName(ORDER_ADJUSTMENT).build();
        Event confirmed = Event.builder().eventName(ORDER_CONFIRMED).build();
        Event onTheRoad = Event.builder().eventName(ORDER_ON_THE_ROUTE).build();
        order.setEvents(List.of(formed, adjustment, confirmed, onTheRoad));
        order.setPayment(TEST_PAYMENT_LIST);
        order.setPointsToUse(0);
        order.setCertificates(Collections.emptySet());
        Set<NotificationParameter> parameters = new HashSet<>();

        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.DONE_OR_CANCELED_UNPAID_ORDER);
        notification.setUser(user);
        notification.setOrder(order);

        when(userNotificationRepository.save(any())).thenReturn(notification);
        when(notificationParameterRepository.saveAll(any())).thenReturn(new ArrayList<>(parameters));
        when(orderBagService.findAllBagsByOrderId(any())).thenReturn(getBag4list());

        notificationService.notifyHalfPaidPackage(order);

        verify(userNotificationRepository).save(any());
        verify(notificationParameterRepository).saveAll(any());
    }

    @Test
    void testNotifyHalfPaidOrderForBroughtByHimself() {
        User user = getUser();
        Order order = ModelUtils.getOrdersStatusBROUGHT_IT_HIMSELFDto();
        order.setConfirmedQuantity(Collections.singletonMap(1, 1));
        order.setExportedQuantity(Collections.emptyMap());
        order.setEvents(List.of(Event.builder().eventName(ORDER_FORMED).build()));
        order.setPayment(TEST_PAYMENT_LIST);
        order.setPointsToUse(0);
        order.setCertificates(Collections.emptySet());
        Set<NotificationParameter> parameters = new HashSet<>();

        UserNotification notification = new UserNotification();
        notification.setNotificationType(NotificationType.ORDER_STATUS_CHANGED);
        notification.setUser(user);
        notification.setOrder(order);

        when(userNotificationRepository.save(any())).thenReturn(notification);
        when(notificationParameterRepository.saveAll(any())).thenReturn(new ArrayList<>(parameters));
        when(orderBagService.findAllBagsByOrderId(any())).thenReturn(getBag4list());

        notificationService.notifyHalfPaidPackage(order);

        verify(userNotificationRepository).save(any());
        verify(notificationParameterRepository).saveAll(any());
    }

    @Test
    void createNotificationDtoTitleUaLanguageTest() {
        String language = "ua";

        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(any(), any()))
            .thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));

        NotificationDto result = NotificationServiceImpl.createNotificationDto(TEST_USER_NOTIFICATION, language,
            NotificationReceiverType.MOBILE, templateRepository, 5L);

        assertEquals(TEST_NOTIFICATION_TEMPLATE.getTitle(), result.getTitle());
    }

    @Test
    void createNotificationDtoTitleEnLanguageTest() {
        String language = "en";

        when(templateRepository.findNotificationTemplateByNotificationTypeAndNotificationReceiverType(any(), any()))
            .thenReturn(Optional.of(TEST_NOTIFICATION_TEMPLATE));

        NotificationDto result = NotificationServiceImpl.createNotificationDto(TEST_USER_NOTIFICATION, language,
            NotificationReceiverType.MOBILE, templateRepository, 5L);

        assertEquals(TEST_NOTIFICATION_TEMPLATE.getTitleEng(), result.getTitle());
    }

    @Test
    void createNotificationDtoOfCustomTemplateTest() {
        String language = "en";

        var testNotificationTemplate = TEST_NOTIFICATION_TEMPLATE;
        testNotificationTemplate.setNotificationType(NotificationType.CUSTOM);
        testNotificationTemplate.setTrigger(NotificationTrigger.CUSTOM);

        var testUserNotification = TEST_USER_NOTIFICATION;
        testUserNotification.setNotificationType(NotificationType.CUSTOM);
        testUserNotification.setTemplateId(testNotificationTemplate.getId());

        when(templateRepository.findNotificationTemplateByIdAndNotificationReceiverType(any(), any()))
            .thenReturn(Optional.of(testNotificationTemplate));

        NotificationDto result = NotificationServiceImpl.createNotificationDto(testUserNotification, language,
            NotificationReceiverType.MOBILE, templateRepository, 5L);

        assertEquals(testNotificationTemplate.getTitleEng(), result.getTitle());
        verify(templateRepository).findNotificationTemplateByIdAndNotificationReceiverType(any(), any());
    }

    @Test
    void notifySelfPickupOrderTest() {
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

        notificationService.notifySelfPickupOrder(order);

        verify(userNotificationRepository).save(notification);
        verify(notificationParameterRepository).saveAll(parameters);
    }

    @Test
    void notifyCreateNewOrderTest() {
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

        notificationService.notifyCreatedOrder(newOrder);

        verify(userNotificationRepository).save(notification);
        verify(notificationParameterRepository).saveAll(parameters);
    }
}