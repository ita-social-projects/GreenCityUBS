package greencity.service;

import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.NotificationShortDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.entity.enums.NotificationReceiverType;
import greencity.entity.enums.NotificationType;
import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.PaymentStatus;
import greencity.entity.notifications.NotificationParameter;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.notifications.UserNotification;
import greencity.entity.order.Bag;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.exceptions.http.NotFoundException;
import greencity.exceptions.notification.NotificationNotFoundException;
import greencity.repository.*;
import greencity.service.notification.AbstractNotificationProvider;
import greencity.service.ubs.NotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static greencity.constant.ErrorMessage.*;
import static greencity.entity.enums.NotificationReceiverType.SITE;
import static java.util.stream.Collectors.toMap;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private UserRepository userRepository;
    private UserNotificationRepository userNotificationRepository;
    private BagRepository bagRepository;
    private OrderRepository orderRepository;
    private ViolationRepository violationRepository;
    private NotificationParameterRepository notificationParameterRepository;
    private Clock clock;
    private List<? extends AbstractNotificationProvider> notificationProviders;
    private final NotificationTemplateRepository templateRepository;

    @Autowired
    @Qualifier("singleThreadedExecutor")
    private ExecutorService executor;

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyUnpaidOrders() {
        for (Order order : orderRepository.findAllByOrderPaymentStatus(OrderPaymentStatus.UNPAID)) {
            Optional<UserNotification> lastNotification = userNotificationRepository
                .findLastNotificationByNotificationTypeAndOrderNumber(NotificationType.UNPAID_ORDER.toString(),
                    order.getId().toString());
            if ((lastNotification.isEmpty()
                || (lastNotification.get().getNotificationTime()
                    .isBefore(LocalDateTime.now(clock).minusDays(7))
                    || lastNotification.get().getNotificationTime().isEqual(LocalDateTime.now(clock).minusDays(7))))
                && (order.getOrderDate().isAfter(LocalDateTime.now(clock).minusMonths(1))
                    || order.getOrderDate().isEqual(LocalDateTime.now(clock).minusMonths(1)))
                && (order.getOrderDate().isBefore(LocalDateTime.now(clock).minusDays(3))
                    || order.getOrderDate().isEqual(LocalDateTime.now(clock).minusDays(3)))) {
                UserNotification userNotification = new UserNotification();
                if (lastNotification.isPresent()) {
                    UserNotification oldNotification = lastNotification.get();
                    userNotification.setUser(oldNotification.getUser());
                } else {
                    userNotification.setUser(order.getUser());
                }
                userNotification.setNotificationTime(LocalDateTime.now(clock));
                userNotification.setNotificationType(NotificationType.UNPAID_ORDER);
                userNotification.setOrder(order);
                UserNotification created = userNotificationRepository.save(userNotification);
                NotificationParameter notificationParameter = new NotificationParameter("orderNumber", order.getId()
                    .toString());
                notificationParameter.setUserNotification(created);
                NotificationParameter createdParameter = notificationParameterRepository.save(notificationParameter);
                created.setParameters(Collections.singleton(createdParameter));
                sendNotificationsForBotsAndEmail(created);
            }
        }
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
        sendNotificationsForBotsAndEmail(notification);
    }

    @Override
    public void notifyPaidOrder(PaymentResponseDto dto) {
        if (dto.getOrder_id() != null) {
            Long orderId = Long.valueOf(dto.getOrder_id().split("_")[0]);
            Optional<Order> orderOptional = orderRepository.findById(orderId);
            if (orderOptional.isPresent()) {
                notifyPaidOrder(orderOptional.get());
            }
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
        Set<NotificationParameter> parameters = new HashSet<>();

        Long paidAmount = order.getPayment().stream()
            .filter(x -> !x.getPaymentStatus().equals(PaymentStatus.PAYMENT_REFUNDED))
            .map(Payment::getAmount).reduce(0L, Long::sum);

        List<Bag> bags = bagRepository.findBagByOrderId(order.getId());
        Map<Integer, Integer> amountOfBagsOrdered = order.getAmountOfBagsOrdered();

        Integer price = bags.stream().map(bag -> amountOfBagsOrdered.get(bag.getId()) * bag.getFullPrice())
            .reduce(0, Integer::sum);

        long amountToPay = price - paidAmount;

        parameters.add(NotificationParameter.builder().key("amountToPay")
            .value(String.format("%.2f", (double) amountToPay)).build());
        parameters.add(NotificationParameter.builder().key("orderNumber")
            .value(order.getId().toString()).build());
        fillAndSendNotification(parameters, order, NotificationType.UNPAID_PACKAGE);
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
            if ((lastNotification.isEmpty()
                || lastNotification.get().getNotificationTime().isBefore(LocalDateTime.now(clock).minusWeeks(1)))
                && order.getOrderDate().isAfter(LocalDateTime.now(clock).minusMonths(1))) {
                notifyHalfPaidPackage(order);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyBonuses(Order order, Long overpayment) {
        Set<NotificationParameter> parameters = new HashSet<>();

        Integer paidBags = order.getConfirmedQuantity().values().stream()
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
        List<User> users = userRepository
            .getAllInactiveUsers(LocalDate.now(clock).minusYears(1), LocalDate.now(clock).minusMonths(2));
        log.info("Found {} inactive users", users.size());
        for (User user : users) {
            Optional<UserNotification> lastNotification =
                userNotificationRepository
                    .findTop1UserNotificationByUserAndNotificationTypeOrderByNotificationTimeDesc(user,
                        NotificationType.LETS_STAY_CONNECTED);
            if (lastNotification.isEmpty()
                || lastNotification.get().getNotificationTime().isBefore(LocalDateTime.now(clock).minusWeeks(1))) {
                UserNotification userNotification = new UserNotification();
                userNotification.setNotificationType(NotificationType.LETS_STAY_CONNECTED);
                userNotification.setUser(user);
                userNotification.setNotificationTime(LocalDateTime.now(clock));
                UserNotification notification = userNotificationRepository.save(userNotification);
                sendNotificationsForBotsAndEmail(notification);
            }
        }
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
            .orElseThrow(() -> new NotificationNotFoundException(NOTIFICATION_DOES_NOT_EXIST));

        if (!notification.getUser().getUuid().equals(uuid)) {
            throw new NotificationNotFoundException(NOTIFICATION_DOES_NOT_BELONG_TO_USER);
        }

        if (!notification.isRead()) {
            notification.setRead(true);
        }

        return createNotificationDto(notification, language, SITE, templateRepository);
    }

    private NotificationShortDto createNotificationShortDto(UserNotification notification, String language) {
        NotificationTemplate template = templateRepository
            .findNotificationTemplateByNotificationTypeAndLanguageCodeAndNotificationReceiverType(
                notification.getNotificationType(),
                language, SITE)
            .orElseThrow(() -> new NotFoundException("Template not found"));

        Long orderId = Objects.nonNull(notification.getOrder()) ? notification.getOrder().getId() : null;

        return NotificationShortDto.builder()
            .id(notification.getId())
            .title(template.getTitle())
            .notificationTime(notification.getNotificationTime())
            .read(notification.isRead())
            .orderId(orderId)
            .build();
    }

    private void sendNotificationsForBotsAndEmail(UserNotification notification) {
        executor.execute(() -> notificationProviders.forEach(provider -> provider.sendNotification(notification)));
    }

    /**
     * Creating notification with parameters.
     */
    public static NotificationDto createNotificationDto(UserNotification notification, String language,
        NotificationReceiverType receiverType,
        NotificationTemplateRepository templateRepository) {
        NotificationTemplate template = templateRepository
            .findNotificationTemplateByNotificationTypeAndLanguageCodeAndNotificationReceiverType(
                notification.getNotificationType(),
                language, receiverType)
            .orElseThrow(() -> new NotFoundException("Template not found"));
        String templateBody = template.getBody();
        if (notification.getParameters() == null) {
            notification.setParameters(Collections.emptySet());
        }
        Map<String, String> valuesMap = notification.getParameters().stream()
            .collect(toMap(NotificationParameter::getKey, NotificationParameter::getValue));

        StringSubstitutor sub = new StringSubstitutor(valuesMap);
        String resultBody = sub.replace(templateBody);

        return NotificationDto.builder().title(template.getTitle())
            .body(resultBody).build();
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
        sendNotificationsForBotsAndEmail(created);
    }
}
