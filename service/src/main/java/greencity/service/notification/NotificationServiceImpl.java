package greencity.service.notification;

import greencity.constant.OrderHistory;
import greencity.dto.notification.InactiveAccountDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.NotificationShortDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.entity.notifications.NotificationPlatform;
import greencity.entity.order.*;
import greencity.enums.*;
import greencity.entity.notifications.NotificationParameter;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.notifications.UserNotification;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.repository.*;
import greencity.service.ubs.NotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static greencity.constant.ErrorMessage.*;
import static greencity.enums.NotificationReceiverType.*;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;

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

    private final Environment environment;

    private static final String ORDER_NUMBER_KEY = "orderNumber";
    private static final String AMOUNT_TO_PAY_KEY = "amountToPay";
    private static final String PAY_BUTTON = "payButton";
    private static final String ORDER_URL_PROPERTY = "greencity.ubs.unpaid-order-url";

    private final String orderUrl;

    /**
     * Class constructor used for autowiring and orderUrl field initialization.
     */
    @Autowired
    public NotificationServiceImpl(UserRepository userRepository, UserNotificationRepository userNotificationRepository,
        BagRepository bagRepository, OrderRepository orderRepository, ViolationRepository violationRepository,
        NotificationParameterRepository notificationParameterRepository,
        List<? extends AbstractNotificationProvider> notificationProviders,
        NotificationTemplateRepository templateRepository, Environment environment) {
        this.userRepository = userRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.bagRepository = bagRepository;
        this.orderRepository = orderRepository;
        this.violationRepository = violationRepository;
        this.notificationParameterRepository = notificationParameterRepository;
        this.notificationProviders = notificationProviders;
        this.templateRepository = templateRepository;
        this.environment = environment;
        this.orderUrl = environment.getProperty(ORDER_URL_PROPERTY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyUnpaidOrders() {
        for (Order order : orderRepository.findAllByOrderPaymentStatus(OrderPaymentStatus.UNPAID)) {
            Optional<UserNotification> lastNotification = userNotificationRepository
                .findLastNotificationByNotificationTypeAndOrderNumber(NotificationType.UNPAID_ORDER.toString(),
                    order.getId().toString());
            if (checkIfUnpaidOrderNeedsNewNotification(order, lastNotification)) {
                UserNotification userNotification = new UserNotification();
                userNotification.setUser(order.getUser());
                double amountToPay = getAmountToPay(order);
                Set<NotificationParameter> notificationParameters =
                    initialiseNotificationParametersForUnpaidOrder(order, amountToPay);
                fillAndSendNotification(notificationParameters, order, NotificationType.UNPAID_ORDER);
            }
        }
    }

    private boolean checkIfUnpaidOrderNeedsNewNotification(Order order, Optional<UserNotification> lastNotification) {
        return (lastNotification.isEmpty()
            || (lastNotification.get().getNotificationTime().isBefore(LocalDateTime.now(clock).minusDays(7))
                || lastNotification.get().getNotificationTime().isEqual(LocalDateTime.now(clock).minusDays(7))))
            && (order.getOrderDate().isAfter(LocalDateTime.now(clock).minusMonths(1))
                || order.getOrderDate().isEqual(LocalDateTime.now(clock).minusMonths(1)))
            && (order.getOrderDate().isBefore(LocalDateTime.now(clock).minusDays(3))
                || order.getOrderDate().isEqual(LocalDateTime.now(clock).minusDays(3)));
    }

    private Set<NotificationParameter> initialiseNotificationParametersForUnpaidOrder(Order order, double amountToPay) {
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
            .value(orderUrl)
            .build());

        return parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyPaidOrder(Order order) {
        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.ORDER_IS_PAID);
        userNotification.setUser(order.getUser());
        userNotification.setOrder(order);
        UserNotification notification = userNotificationRepository.save(userNotification);
        sendNotificationsForBotsAndEmail(notification, 0L);
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
    public void notifyCourierItineraryFormed(Order order) {
        Set<NotificationParameter> parameters = new HashSet<>();
        parameters.add(NotificationParameter.builder().key("date")
            .value(order.getDeliverFrom() != null ? order.getDeliverFrom().format(DateTimeFormatter.ofPattern("dd-MM"))
                : "")
            .build());
        parameters.add(NotificationParameter.builder().key("startTime")
            .value(order.getDeliverFrom() != null ? order.getDeliverFrom().format(DateTimeFormatter.ofPattern("hh:mm"))
                : "")
            .build());
        parameters.add(NotificationParameter.builder().key("endTime")
            .value(
                order.getDeliverTo() != null ? order.getDeliverTo().format(DateTimeFormatter.ofPattern("hh:mm")) : "")
            .build());
        parameters.add(NotificationParameter.builder().key("phoneNumber")
            .value("+380638175035, +380931038987").build());
        fillAndSendNotification(parameters, order, NotificationType.COURIER_ITINERARY_FORMED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyHalfPaidPackage(Order order) {
        double amountToPay = getAmountToPay(order);
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
        double amountToPay = getAmountToPay(order);
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

    private double getAmountToPay(Order order) {
        long bonuses = order.getPointsToUse() == null ? 0L : order.getPointsToUse().longValue();
        long certificates = order.getCertificates() == null ? 0L
            : order.getCertificates().stream()
                .map(Certificate::getPoints)
                .reduce(0, Integer::sum)
                .longValue();

        double coinsInOneUah = 100.0;
        double paidAmount = order.getPayment() == null ? 0d
            : order.getPayment().stream()
                .filter(payment -> payment.getPaymentStatus() == PaymentStatus.PAID)
                .map(Payment::getAmount)
                .reduce(0L, Long::sum) / coinsInOneUah;

        long ubsCourierSum = order.getUbsCourierSum() == null ? 0L : order.getUbsCourierSum();
        long writeStationSum = order.getWriteOffStationSum() == null ? 0L : order.getWriteOffStationSum();

        List<Bag> bagsType = bagRepository.findBagsByOrderId(order.getId());
        Map<Integer, Integer> bagsAmount;
        if (MapUtils.isNotEmpty(order.getExportedQuantity())) {
            bagsAmount = order.getExportedQuantity();
        } else if (MapUtils.isNotEmpty(order.getConfirmedQuantity())) {
            bagsAmount = order.getConfirmedQuantity();
        } else {
            bagsAmount = order.getAmountOfBagsOrdered();
        }

        long totalPrice = bagsAmount.entrySet().stream()
            .map(entry -> entry.getValue() * getBagPrice(entry.getKey(), bagsType))
            .reduce(0, Integer::sum)
            .longValue();

        return totalPrice - paidAmount - bonuses - certificates + ubsCourierSum + writeStationSum;
    }

    private int getBagPrice(Integer bagId, List<Bag> bagsType) {
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
                .findLastNotificationByNotificationTypeAndOrderNumber(
                    NotificationType.UNPAID_PACKAGE.toString(),
                    order.getId().toString());
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

        parameters.add(NotificationParameter.builder().key("overpayment")
            .value(String.valueOf(overpayment)).build());
        parameters.add(NotificationParameter.builder().key("realPackageNumber")
            .value(exportedBags.toString()).build());
        parameters.add(NotificationParameter.builder().key("paidPackageNumber")
            .value(paidBags.toString()).build());
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
        parameters.add(NotificationParameter.builder().key("returnedPayment")
            .value(String.valueOf(order.getPointsToUse())).build());

        fillAndSendNotification(parameters, order, NotificationType.BONUSES_FROM_CANCELLED_ORDER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyAddViolation(Long orderId) {
        Set<NotificationParameter> parameters = new HashSet<>();
        Violation violation = violationRepository.findByOrderId(orderId)
            .orElseThrow(() -> new NotFoundException(VIOLATION_DOES_NOT_EXIST));
        parameters.add(NotificationParameter.builder()
            .key("violationDescription")
            .value(violation.getDescription()).build());
        fillAndSendNotification(parameters, violation.getOrder(), NotificationType.VIOLATION_THE_RULES);
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
            .forEach(provider -> {
                provider.sendNotification(notification, MOBILE, monthsOfAccountInactivity);
                provider.sendNotification(notification, EMAIL, monthsOfAccountInactivity);
            }));
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
