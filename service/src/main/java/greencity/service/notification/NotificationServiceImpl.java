package greencity.service.notification;

import greencity.config.InternalUrlConfigProp;
import greencity.constant.AppConstant;
import greencity.constant.OrderHistory;
import greencity.dto.notification.InactiveAccountDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.NotificationShortDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.entity.notifications.NotificationPlatform;
import greencity.entity.order.Bag;
import greencity.entity.order.Order;
import greencity.entity.order.Certificate;
import greencity.entity.order.Event;
import greencity.entity.order.Payment;
import greencity.entity.notifications.NotificationParameter;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.enums.NotificationReceiverType;
import greencity.enums.NotificationType;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.repository.BagRepository;
import greencity.repository.NotificationParameterRepository;
import greencity.repository.NotificationTemplateRepository;
import greencity.repository.OrderRepository;
import greencity.repository.UserNotificationRepository;
import greencity.repository.UserRepository;
import greencity.repository.ViolationRepository;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.OrderBagService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import static greencity.constant.ErrorMessage.BAG_NOT_FOUND;
import static greencity.constant.ErrorMessage.NOTIFICATION_DOES_NOT_BELONG_TO_USER;
import static greencity.constant.ErrorMessage.NOTIFICATION_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.VIOLATION_DOES_NOT_EXIST;
import static greencity.enums.NotificationReceiverType.SITE;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;
import static greencity.constant.OrderHistory.ADD_VIOLATION;
import static greencity.constant.OrderHistory.CHANGES_VIOLATION;
import static greencity.constant.OrderHistory.DELETE_VIOLATION;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final UserRepository userRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final BagRepository bagRepository;
    private final OrderRepository orderRepository;
    private final ViolationRepository violationRepository;
    private final NotificationParameterRepository notificationParameterRepository;
    @Autowired
    @Qualifier("kyivZonedClock")
    private Clock clock;
    private final List<? extends AbstractNotificationProvider> notificationProviders;
    private final NotificationTemplateRepository templateRepository;
    @Autowired
    @Qualifier("singleThreadedExecutor")
    private ExecutorService executor;
    private final InternalUrlConfigProp internalUrlConfigProp;

    private static final String ORDER_NUMBER_KEY = "orderNumber";
    private static final String AMOUNT_TO_PAY_KEY = "amountToPay";
    private static final String PAY_BUTTON = "payButton";
    private static final String VIOLATION_DESCRIPTION = "violationDescription";

    private static final int MAX_NOTIFICATION_ORDER_AGE_MONTHS = 1;
    private static final int MIN_NOTIFICATION_ORDER_AGE_DAYS = 3;
    private static final int MAX_NOTIFICATIONS_PER_WEEK = 1;

    @Autowired
    private final OrderBagService orderBagService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyUnpaidOrders() {
        for (Order order : orderRepository.findAllByOrderPaymentStatus(OrderPaymentStatus.UNPAID)) {
            if (checkIfOrderNeedsNewNotification(order, NotificationType.UNPAID_ORDER)) {
                UserNotification userNotification = new UserNotification();
                userNotification.setUser(order.getUser());
                Double amountToPay = getAmountToPay(order);
                Set<NotificationParameter> notificationParameters =
                    initialiseNotificationParametersForUnpaidOrder(order, amountToPay);
                fillAndSendNotification(notificationParameters, order, NotificationType.UNPAID_ORDER);
            }
        }
    }

    private Set<NotificationParameter> initialiseNotificationParametersForUnpaidOrder(Order order,
        Double amountToPay) {
        Set<NotificationParameter> parameters = new HashSet<>();

        parameters.add(NotificationParameter.builder()
            .key(AMOUNT_TO_PAY_KEY)
            .value(String.format("%.2f", amountToPay))
            .build());

        parameters.add(NotificationParameter.builder()
            .key(ORDER_NUMBER_KEY)
            .value(order.getId().toString())
            .build());

        parameters.add(NotificationParameter.builder()
            .key(PAY_BUTTON)
            .value(internalUrlConfigProp.getOrderUrl() + "?existingOrderId=" + order.getId())
            .build());

        return parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyPaidOrder(Order order) {
        NotificationParameter orderNumber = NotificationParameter.builder()
            .key(ORDER_NUMBER_KEY)
            .value(order.getId().toString())
            .build();
        fillAndSendNotification(Set.of(orderNumber), order, NotificationType.ORDER_IS_PAID);
    }

    @Override
    public void notifyPaidOrder(PaymentResponseDto dto) {
        if (dto.getOrder_id() != null) {
            Long orderId = Long.valueOf(dto.getOrder_id().split("_")[0]);
            Optional<Order> orderOptional = orderRepository.findById(orderId);
            orderOptional.ifPresent(this::notifyPaidOrder);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyAllCourierItineraryFormed() {
        var orders =
            orderRepository.findAllByOrderStatusAndOrderPaymentStatus(OrderStatus.ADJUSTMENT, OrderPaymentStatus.PAID);
        orders.forEach(order -> {
            checkIfOrderNeedsNewNotification(order, NotificationType.COURIER_ITINERARY_FORMED);
            notifyCourierItineraryFormed(order);
        });
    }

    @Override
    public void notifyCourierItineraryFormed(Order order) {
        Set<NotificationParameter> parameters = new HashSet<>();
        parameters.add(NotificationParameter.builder()
            .key("date")
            .value(order.getDeliverFrom() != null ? order.getDeliverFrom().format(DateTimeFormatter.ofPattern("dd-MM"))
                : "")
            .build());
        parameters.add(NotificationParameter.builder()
            .key("startTime")
            .value(order.getDeliverFrom() != null ? order.getDeliverFrom().format(DateTimeFormatter.ofPattern("hh:mm"))
                : "")
            .build());
        parameters.add(NotificationParameter.builder()
            .key("endTime")
            .value(
                order.getDeliverTo() != null ? order.getDeliverTo().format(DateTimeFormatter.ofPattern("hh:mm")) : "")
            .build());
        parameters.add(NotificationParameter.builder()
            .key("phoneNumber")
            .value("+380638175035, +380931038987")
            .build());
        parameters.add(NotificationParameter.builder()
            .key(ORDER_NUMBER_KEY)
            .value(order.getId().toString())
            .build());
        fillAndSendNotification(parameters, order, NotificationType.COURIER_ITINERARY_FORMED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyHalfPaidPackage(Order order) {
        Double amountToPay = getAmountToPay(order);
        Set<NotificationParameter> parameters = initialiseNotificationParametersForUnpaidOrder(order, amountToPay);

        if (order.getOrderStatus() == OrderStatus.BROUGHT_IT_HIMSELF) {
            fillAndSendNotification(parameters, order, NotificationType.HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF);
        } else if ((order.getOrderStatus() == OrderStatus.DONE || order.getOrderStatus() == OrderStatus.CANCELED)
            && order.getEvents().stream()
                .map(Event::getEventName)
                .filter(e -> e.equals(OrderHistory.ORDER_ADJUSTMENT) || e.equals(OrderHistory.ORDER_CONFIRMED)
                    || e.equals(OrderHistory.ORDER_ON_THE_ROUTE) || e.equals(OrderHistory.ORDER_NOT_TAKEN_OUT))
                .count() == 3) {
            fillAndSendNotification(parameters, order, NotificationType.DONE_OR_CANCELED_UNPAID_ORDER);
        } else {
            fillAndSendNotification(parameters, order, NotificationType.UNPAID_PACKAGE);
        }
    }

    @Override
    public void notifyUnpaidOrder(Order order) {
        Double amountToPay = getAmountToPay(order);
        Set<NotificationParameter> parameters = initialiseNotificationParametersForUnpaidOrder(order, amountToPay);

        if (order.getOrderStatus() == OrderStatus.BROUGHT_IT_HIMSELF
            && order.getEvents().stream()
                .map(Event::getEventName)
                .noneMatch(e -> e.equals(OrderHistory.ORDER_ADJUSTMENT) || e.equals(OrderHistory.ORDER_CONFIRMED))) {
            fillAndSendNotification(parameters, order, NotificationType.ORDER_STATUS_CHANGED);
        } else if ((order.getOrderStatus() == OrderStatus.DONE || order.getOrderStatus() == OrderStatus.CANCELED)
            && order.getEvents().stream()
                .map(Event::getEventName)
                .filter(e -> e.equals(OrderHistory.ORDER_ADJUSTMENT) || e.equals(OrderHistory.ORDER_CONFIRMED)
                    || e.equals(OrderHistory.ORDER_ON_THE_ROUTE) || e.equals(OrderHistory.ORDER_NOT_TAKEN_OUT))
                .count() == 3) {
            fillAndSendNotification(parameters, order, NotificationType.DONE_OR_CANCELED_UNPAID_ORDER);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifySelfPickupOrder(Order order) {
        Set<NotificationParameter> parameters = Set.of(NotificationParameter.builder()
            .key(ORDER_NUMBER_KEY)
            .value(order.getId().toString())
            .build());
        fillAndSendNotification(parameters, order, NotificationType.ORDER_STATUS_CHANGED);
    }

    private Double getAmountToPay(Order order) {
        long bonusesInCoins = order.getPointsToUse() == null ? 0L : order.getPointsToUse() * 100L;
        long certificatesInCoins = order.getCertificates() == null ? 0L
            : 100L * order.getCertificates().stream()
                .map(Certificate::getPoints)
                .reduce(0, Integer::sum);

        long paidAmountInCoins = order.getPayment() == null ? 0L
            : order.getPayment().stream()
                .filter(payment -> payment.getPaymentStatus() == PaymentStatus.PAID)
                .map(Payment::getAmount)
                .reduce(0L, Long::sum);

        long ubsCourierSumInCoins = order.getUbsCourierSum() == null ? 0L : order.getUbsCourierSum();
        long writeStationSumInCoins = order.getWriteOffStationSum() == null ? 0L : order.getWriteOffStationSum();

        List<Bag> bagsType = orderBagService.findAllBagsByOrderId(order.getId());
        Map<Integer, Integer> bagsAmount;
        if (MapUtils.isNotEmpty(order.getExportedQuantity())) {
            bagsAmount = order.getExportedQuantity();
        } else if (MapUtils.isNotEmpty(order.getConfirmedQuantity())) {
            bagsAmount = order.getConfirmedQuantity();
        } else {
            bagsAmount = order.getAmountOfBagsOrdered();
        }

        long totalPriceInCoins = bagsAmount.entrySet().stream()
            .map(entry -> entry.getValue() * getBagPrice(entry.getKey(), bagsType))
            .reduce(0L, Long::sum);

        long unPaidAmountInCoins = totalPriceInCoins - bonusesInCoins - certificatesInCoins + ubsCourierSumInCoins
            + writeStationSumInCoins;
        return BigDecimal.valueOf(unPaidAmountInCoins - paidAmountInCoins)
            .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
            .setScale(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY, RoundingMode.HALF_UP).doubleValue();
    }

    private long getBagPrice(Integer bagId, List<Bag> bagsType) {
        return bagsType.stream()
            .filter(b -> b.getId().equals(bagId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(BAG_NOT_FOUND + bagId))
            .getFullPrice();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyAllHalfPaidPackages() {
        for (Order order : orderRepository.findAllByOrderPaymentStatus(OrderPaymentStatus.HALF_PAID)) {
            Optional<UserNotification> lastNotification = userNotificationRepository
                .findFirstByOrderIdAndNotificationTypeInOrderByNotificationTimeDesc(
                    order.getId(),
                    NotificationType.UNPAID_PACKAGE,
                    NotificationType.HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF,
                    NotificationType.DONE_OR_CANCELED_UNPAID_ORDER);
            if (checkIfHalfPaidPackageNeedsNotification(order, lastNotification)) {
                notifyHalfPaidPackage(order);
            }
        }
    }

    private boolean checkIfHalfPaidPackageNeedsNotification(Order order, Optional<UserNotification> lastNotification) {
        return (lastNotification.isEmpty()
            || lastNotification.get().getNotificationTime().isBefore(LocalDateTime.now(clock).minusWeeks(1)))
            && order.getOrderDate().isAfter(LocalDateTime.now(clock).minusMonths(1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyBonuses(Order order, Long overpayment) {
        if (overpayment <= 0) {
            return;
        }

        Set<NotificationParameter> parameters = new HashSet<>();

        Integer paidBags = order.getAmountOfBagsOrdered().values().stream()
            .reduce(0, Integer::sum);
        Integer exportedBags = order.getExportedQuantity().values().stream()
            .reduce(0, Integer::sum);

        parameters.add(NotificationParameter.builder()
            .key("overpayment")
            .value(String.valueOf(overpayment))
            .build());
        parameters.add(NotificationParameter.builder()
            .key("realPackageNumber")
            .value(exportedBags.toString())
            .build());
        parameters.add(NotificationParameter.builder()
            .key("paidPackageNumber")
            .value(paidBags.toString())
            .build());
        parameters.add(NotificationParameter.builder()
            .key(ORDER_NUMBER_KEY)
            .value(order.getId().toString())
            .build());
        fillAndSendNotification(parameters, order, NotificationType.ACCRUED_BONUSES_TO_ACCOUNT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyBonusesFromCanceledOrder(Order order) {
        Set<NotificationParameter> parameters = new HashSet<>();
        Integer pointsToReturn = order.getPointsToUse();
        if (isNull(pointsToReturn) || pointsToReturn == 0) {
            return;
        }
        parameters.add(NotificationParameter.builder()
            .key("returnedPayment")
            .value(String.valueOf(order.getPointsToUse())).build());
        parameters.add(NotificationParameter.builder()
            .key(ORDER_NUMBER_KEY)
            .value(order.getId().toString())
            .build());
        fillAndSendNotification(parameters, order, NotificationType.BONUSES_FROM_CANCELLED_ORDER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyAddViolation(Long orderId) {
        Violation violation = violationRepository.findByOrderId(orderId)
            .orElseThrow(() -> new NotFoundException(VIOLATION_DOES_NOT_EXIST));
        createNewViolationParametersAndSend(orderId, violation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyChangedViolation(Violation violation, Long orderId) {
        Set<NotificationParameter> parameters = new HashSet<>();
        parameters.add(NotificationParameter.builder()
            .key(ORDER_NUMBER_KEY)
            .value(orderId.toString())
            .build());
        fillAndSendNotification(parameters, violation.getOrder(), NotificationType.CHANGED_IN_RULE_VIOLATION_STATUS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDeleteViolation(Long orderId) {
        Set<NotificationParameter> parameters = new HashSet<>();
        Violation violation = violationRepository.findByOrderId(orderId)
            .orElseThrow(() -> new NotFoundException(VIOLATION_DOES_NOT_EXIST));
        parameters.add(NotificationParameter.builder()
            .key(ORDER_NUMBER_KEY)
            .value(orderId.toString())
            .build());
        fillAndSendNotification(parameters, violation.getOrder(),
            NotificationType.CANCELED_VIOLATION_THE_RULES_BY_THE_MANAGER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyAllAddedViolations() {
        var orders = getOrdersWithNewViolations();
        notifyNewViolations(orders);
    }

    private List<Order> getOrdersWithNewViolations() {
        return orderRepository.findAllWithEventsByEventNames(ADD_VIOLATION, CHANGES_VIOLATION, DELETE_VIOLATION)
            .stream()
            .filter(order -> orderHasJustNewViolations(order.getEvents()))
            .toList();
    }

    private boolean orderHasJustNewViolations(List<Event> events) {
        var names = events.stream()
            .map(Event::getEventName)
            .toList();

        return names.contains(ADD_VIOLATION)
            && !names.contains(CHANGES_VIOLATION) && !names.contains(DELETE_VIOLATION);
    }

    private void notifyNewViolations(List<Order> orders) {
        orders.forEach(order -> {
            checkIfOrderNeedsNewNotification(order, NotificationType.VIOLATION_THE_RULES);
            violationRepository.findByOrderId(order.getId())
                .ifPresent(violation -> createNewViolationParametersAndSend(order.getId(), violation));
        });
    }

    private void createNewViolationParametersAndSend(Long orderId, Violation violation) {
        Set<NotificationParameter> parameters = new HashSet<>();
        parameters.add(NotificationParameter.builder()
            .key(VIOLATION_DESCRIPTION)
            .value(violation.getDescription())
            .build());
        parameters.add(NotificationParameter.builder()
            .key(ORDER_NUMBER_KEY)
            .value(orderId.toString())
            .build());
        fillAndSendNotification(parameters, violation.getOrder(), NotificationType.VIOLATION_THE_RULES);
    }

    @Override
    public void notifyAllCanceledViolations() {
        var orders = orderRepository.findAllWithEventsByEventNames(DELETE_VIOLATION);
        notifyViolations(orders, NotificationType.CANCELED_VIOLATION_THE_RULES_BY_THE_MANAGER);
    }

    @Override
    public void notifyAllChangedViolations() {
        var orders = getOrdersWithChangedViolations();
        notifyViolations(orders, NotificationType.CHANGED_IN_RULE_VIOLATION_STATUS);
    }

    private List<Order> getOrdersWithChangedViolations() {
        return orderRepository.findAllWithEventsByEventNames(CHANGES_VIOLATION, DELETE_VIOLATION)
            .stream()
            .filter(o -> orderHasChangedViolations(o.getEvents()))
            .toList();
    }

    private boolean orderHasChangedViolations(List<Event> events) {
        var names = events.stream()
            .map(Event::getEventName)
            .toList();

        return names.contains(CHANGES_VIOLATION) && !names.contains(DELETE_VIOLATION);
    }

    private void notifyViolations(List<Order> orders, NotificationType notificationType) {
        orders.forEach(order -> {
            checkIfOrderNeedsNewNotification(order, notificationType);
            Set<NotificationParameter> parameters = new HashSet<>();
            parameters.add(NotificationParameter.builder()
                .key(ORDER_NUMBER_KEY)
                .value(order.getId().toString())
                .build());
            fillAndSendNotification(parameters, order, notificationType);
        });
    }

    @Override
    public void notifyAllDoneOrCanceledUnpaidOrders() {
        var orders = orderRepository.findAllByPaymentStatusesAndOrderStatuses(
            List.of(OrderPaymentStatus.UNPAID, OrderPaymentStatus.HALF_PAID),
            List.of(OrderStatus.DONE, OrderStatus.CANCELED));
        orders.forEach(this::notifyDoneOrCanceledUnpaidOrder);
    }

    private void notifyDoneOrCanceledUnpaidOrder(Order order) {
        if (checkByEventsOrderDoneOrCanceled(order)) {
            notifyOrderWithStatus(order, NotificationType.DONE_OR_CANCELED_UNPAID_ORDER);
        }
    }

    private boolean checkByEventsOrderDoneOrCanceled(Order order) {
        return order.getEvents().stream()
            .map(Event::getEventName)
            .filter(e -> e.equals(OrderHistory.ORDER_ADJUSTMENT) || e.equals(OrderHistory.ORDER_CONFIRMED)
                || e.equals(OrderHistory.ORDER_ON_THE_ROUTE) || e.equals(OrderHistory.ORDER_NOT_TAKEN_OUT))
            .count() == 3;
    }

    @Override
    public void notifyAllHalfPaidOrdersWithStatusBroughtByHimself() {
        var orders = orderRepository.findAllByOrderStatusAndOrderPaymentStatus(
            OrderStatus.BROUGHT_IT_HIMSELF, OrderPaymentStatus.HALF_PAID);

        orders.forEach(
            order -> notifyOrderWithStatus(order, NotificationType.HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF));
    }

    @Override
    public void notifyAllChangedOrderStatuses() {
        var orders = orderRepository.findAllByOrderStatusWithEvents(OrderStatus.BROUGHT_IT_HIMSELF);
        orders.forEach(order -> {
            if (checkByEventsOrderStatusIsChanged(order)) {
                notifyOrderWithStatus(order, NotificationType.ORDER_STATUS_CHANGED);
            }
        });
    }

    private boolean checkByEventsOrderStatusIsChanged(Order order) {
        return order.getEvents().stream()
            .map(Event::getEventName)
            .noneMatch(e -> e.equals(OrderHistory.ORDER_ADJUSTMENT) || e.equals(OrderHistory.ORDER_CONFIRMED));
    }

    @Override
    public void notifyUnpaidPackages() {
        var orders = orderRepository.findAllByOrderPaymentStatusWithEvents(OrderPaymentStatus.HALF_PAID);
        orders.forEach(this::notifyUnpaidPackage);
    }

    private void notifyUnpaidPackage(Order order) {
        if (checkOrderWithUnpaidPackage(order)) {
            notifyOrderWithStatus(order, NotificationType.UNPAID_PACKAGE);
        }
    }

    private boolean checkOrderWithUnpaidPackage(Order order) {
        return checkOrderIsNotBroughtByHimself(order) && checkOrderIsNotDoneOrCanceled(order);
    }

    private boolean checkOrderIsNotBroughtByHimself(Order order) {
        return order.getOrderStatus() != OrderStatus.BROUGHT_IT_HIMSELF;
    }

    private boolean checkOrderIsNotDoneOrCanceled(Order order) {
        return !((order.getOrderStatus() == OrderStatus.DONE || order.getOrderStatus() == OrderStatus.CANCELED)
            && checkByEventsOrderDoneOrCanceled(order));
    }

    private void notifyOrderWithStatus(Order order, NotificationType notificationType) {
        checkIfOrderNeedsNewNotification(order, notificationType);
        var amountToPay = getAmountToPay(order);
        var parameters = initialiseNotificationParametersForUnpaidOrder(order, amountToPay);
        fillAndSendNotification(parameters, order, notificationType);
    }

    private boolean checkIfOrderNeedsNewNotification(Order order, NotificationType notificationType) {
        Optional<UserNotification> lastNotification = getLastOrderNotificationByType(order, notificationType);
        return checkUserNeedNotification(lastNotification) && checkOrderNeedNotification(order);
    }

    private Optional<UserNotification> getLastOrderNotificationByType(Order order, NotificationType notificationType) {
        return userNotificationRepository
            .findFirstByOrderIdAndNotificationTypeInOrderByNotificationTimeDesc(order.getId(), notificationType);
    }

    private boolean checkUserNeedNotification(Optional<UserNotification> lastNotification) {
        if (lastNotification.isEmpty()) {
            return true;
        }
        LocalDateTime weekAgo = LocalDateTime.now(clock).minusWeeks(MAX_NOTIFICATIONS_PER_WEEK);
        LocalDateTime lastNotificationTime = lastNotification.get().getNotificationTime();

        return lastNotificationTime.isBefore(weekAgo) || lastNotificationTime.isEqual(weekAgo);
    }

    private boolean checkOrderNeedNotification(Order order) {
        return (order.getOrderDate().isAfter(LocalDateTime.now(clock).minusMonths(MAX_NOTIFICATION_ORDER_AGE_MONTHS))
            || order.getOrderDate().isEqual(LocalDateTime.now(clock).minusMonths(MAX_NOTIFICATION_ORDER_AGE_MONTHS)))
            && (order.getOrderDate().isBefore(LocalDateTime.now(clock).minusDays(MIN_NOTIFICATION_ORDER_AGE_DAYS))
                || order.getOrderDate().isEqual(LocalDateTime.now(clock).minusDays(MIN_NOTIFICATION_ORDER_AGE_DAYS)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyInactiveAccounts() {
        Long[] monthsList = {2L, 4L, 6L, 8L, 10L, 12L};
        List<Callable<InactiveAccountDto>> callableGetInactiveUsersTasks = new ArrayList<>();
        LocalDate dateOfLastNotification = LocalDate.now(clock).minusMonths(2L);
        List<Long> userIdsByLastNotifications =
            userNotificationRepository
                .getUserIdByDateOfLastNotificationAndNotificationType(dateOfLastNotification,
                    NotificationType.LETS_STAY_CONNECTED.toString());

        findInactiveUsers(monthsList, callableGetInactiveUsersTasks, userIdsByLastNotifications);
        tryToSendNotificationForInactiveUser(callableGetInactiveUsersTasks);
    }

    private void findInactiveUsers(Long[] monthsList, List<Callable<InactiveAccountDto>> callableGetInactiveUsersTasks,
        List<Long> userIdsByLastNotifications) {
        Arrays.stream(monthsList).forEach(months -> {
            LocalDate dateOfLastOrder = LocalDate.now(clock).minusMonths(months);
            callableGetInactiveUsersTasks.add(() -> {
                List<User> users = userRepository.getInactiveUsersByDateOfLastOrder(dateOfLastOrder);
                List<User> filteredUsers = users.stream()
                    .filter(user -> !userIdsByLastNotifications.contains(user.getId()))
                    .collect(Collectors.toList());
                log.info("Found {} inactive users for {} months ", filteredUsers.size(), months);
                return InactiveAccountDto.builder().users(filteredUsers).months(months).build();
            });
        });
    }

    private void tryToSendNotificationForInactiveUser(
        List<Callable<InactiveAccountDto>> callableGetInactiveUsersTasks) {
        try {
            List<Future<InactiveAccountDto>> futures = executor.invokeAll(callableGetInactiveUsersTasks);
            futures.forEach(future -> {
                try {
                    List<User> users = future.get().getUsers();
                    Long monthsOfAccountInactivity = future.get().getMonths();
                    users.forEach(user -> {
                        UserNotification notification = initialiseNotificationForInactiveUser(user);
                        sendNotificationsForBotsAndEmail(notification, monthsOfAccountInactivity);
                    });
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Unable to send notification to user {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            });
        } catch (InterruptedException e) {
            log.error("Unable to start thread {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private UserNotification initialiseNotificationForInactiveUser(User user) {
        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.LETS_STAY_CONNECTED);
        userNotification.setUser(user);
        userNotification.setNotificationTime(LocalDateTime.now(clock));
        return userNotificationRepository.save(userNotification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<NotificationShortDto> getAllNotificationsForUser(String userUuid,
        String language,
        Pageable pageable) {
        User user = userRepository.findByUuid(userUuid);

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
            Sort.by("notificationTime").descending());

        Page<UserNotification> notifications = userNotificationRepository.findAllByUser(user, pageRequest);

        List<NotificationShortDto> notificationShortDtoList = notifications.stream()
            .map(n -> createNotificationShortDto(n, language))
            .collect(Collectors.toCollection(LinkedList::new));

        return new PageableDto<>(
            notificationShortDtoList,
            notifications.getTotalElements(),
            notifications.getPageable().getPageNumber(),
            notifications.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    public long getUnreadenNotifications(String userUuid) {
        User user = userRepository.findByUuid(userUuid);
        return userNotificationRepository.countUserNotificationByUserAndReadIsFalse(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationDto getNotification(String uuid, Long id, String language) {
        UserNotification notification = userNotificationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(NOTIFICATION_DOES_NOT_EXIST));

        if (!notification.getUser().getUuid().equals(uuid)) {
            throw new AccessDeniedException(NOTIFICATION_DOES_NOT_BELONG_TO_USER);
        }

        if (!notification.isRead()) {
            notification.setRead(true);
        }

        NotificationDto notificationDto = createNotificationDto(notification, language, SITE, templateRepository, 0L);

        if (NotificationType.VIOLATION_THE_RULES.equals(notification.getNotificationType())) {
            Violation violation = violationRepository.findByOrderId(notification.getOrder().getId())
                .orElseThrow(() -> new NotFoundException(VIOLATION_DOES_NOT_EXIST));
            notificationDto.setImages(violation.getImages());
        }

        return notificationDto;
    }

    private NotificationShortDto createNotificationShortDto(UserNotification notification, String language) {
        NotificationTemplate template = templateRepository
            .findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
                notification.getNotificationType(), SITE)
            .orElseThrow(() -> new NotFoundException("Template not found"));

        Long orderId = Objects.nonNull(notification.getOrder()) ? notification.getOrder().getId() : null;

        return NotificationShortDto.builder()
            .id(notification.getId())
            .title(language.equals("ua")
                ? template.getTitle()
                : template.getTitleEng())
            .notificationTime(notification.getNotificationTime())
            .read(notification.isRead())
            .orderId(orderId)
            .build();
    }

    private void sendNotificationsForBotsAndEmail(UserNotification notification, long monthsOfAccountInactivity) {
        executor.execute(() -> notificationProviders
            .forEach(provider -> provider.sendNotification(notification, provider.getNotificationType(),
                monthsOfAccountInactivity)));
    }

    /**
     * Creating notification with parameters.
     */
    public static NotificationDto createNotificationDto(UserNotification notification, String language,
        NotificationReceiverType receiverType,
        NotificationTemplateRepository templateRepository, long monthsOfAccountInactivity) {
        NotificationTemplate template = templateRepository
            .findNotificationTemplateByNotificationTypeAndNotificationReceiverType(
                notification.getNotificationType(), receiverType)
            .orElseThrow(() -> new NotFoundException("Template not found"));
        String templateBody = resolveTemplateBody(language, receiverType, template);
        if (notification.getParameters() == null) {
            notification.setParameters(Collections.emptySet());
        }
        Map<String, String> valuesMap = notification.getParameters().stream()
            .collect(toMap(NotificationParameter::getKey, NotificationParameter::getValue));

        StringSubstitutor sub = new StringSubstitutor(valuesMap);
        String resultBody = sub.replace(String.format(templateBody, monthsOfAccountInactivity));
        String title = language.equals("ua") ? template.getTitle() : template.getTitleEng();

        return NotificationDto.builder().title(title)
            .body(resultBody).build();
    }

    private static String resolveTemplateBody(String language, NotificationReceiverType receiverType,
        NotificationTemplate notification) {
        return language.equals("ua")
            ? getNotificationPlatformByReceiverType(notification, receiverType).getBody()
            : getNotificationPlatformByReceiverType(notification, receiverType).getBodyEng();
    }

    private static NotificationPlatform getNotificationPlatformByReceiverType(
        NotificationTemplate template, NotificationReceiverType receiverType) {
        return template.getNotificationPlatforms().stream()
            .filter(platform -> platform.getNotificationReceiverType() == receiverType)
            .findAny()
            .orElseThrow();
    }

    private void fillAndSendNotification(Set<NotificationParameter> parameters, Order order,
        NotificationType notificationType) {
        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(notificationType);
        userNotification.setUser(order.getUser());
        userNotification.setOrder(order);
        UserNotification created = userNotificationRepository.save(userNotification);
        parameters.forEach(parameter -> parameter.setUserNotification(created));
        List<NotificationParameter> notificationParameters = notificationParameterRepository.saveAll(parameters);
        created.setParameters(new HashSet<>(notificationParameters));
        sendNotificationsForBotsAndEmail(created, 0L);
    }
}