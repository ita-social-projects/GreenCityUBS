package greencity.service;

import greencity.dto.NotificationDto;
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
import greencity.exceptions.NotFoundException;
import greencity.repository.*;
import greencity.service.ubs.NotificationService;
import greencity.ubstelegrambot.TelegramService;
import greencity.ubsviberbot.ViberServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static java.util.stream.Collectors.toMap;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private NotificationTemplateRepository notificationTemplateRepository;
    private UserRepository userRepository;
    private UserNotificationRepository userNotificationRepository;
    private BagRepository bagRepository;
    private OrderRepository orderRepository;
    private ViolationRepository violationRepository;
    private NotificationParameterRepository notificationParameterRepository;
    private Clock clock;
    private ViberServiceImpl viberService;
    private TelegramService telegramService;

    @Autowired
    @Qualifier("singleThreadedExecutor")
    private ExecutorService executor;

    @Override
    public void notifyUnpaidOrders() {
        for (Order order : orderRepository.findAllByOrderPaymentStatus(OrderPaymentStatus.UNPAID)) {
            Optional<UserNotification> lastNotification = userNotificationRepository
                    .findLastNotificationByNotificationTypeAndOrderNumber(NotificationType.UNPAID_ORDER.toString(),
                            order.getId().toString());
            if ((lastNotification.isEmpty()
                    || lastNotification.get().getNotificationTime().isBefore(LocalDateTime.now(clock).minusWeeks(1)))
                    && order.getOrderDate().isAfter(LocalDateTime.now(clock).minusMonths(1))) {
                UserNotification userNotification = new UserNotification();
                if (lastNotification.isPresent()) {
                    UserNotification oldNotification = lastNotification.get();
                    userNotification.setUser(oldNotification.getUser());
                } else {
                    userNotification.setUser(order.getUser());
                }
                userNotification.setNotificationTime(LocalDateTime.now(clock));
                userNotification.setNotificationType(NotificationType.UNPAID_ORDER);
                UserNotification created = userNotificationRepository.save(userNotification);
                NotificationParameter notificationParameter = new NotificationParameter("orderNumber", order.getId()
                        .toString());
                notificationParameter.setUserNotification(created);
                NotificationParameter createdParameter = notificationParameterRepository.save(notificationParameter);
                created.setParameters(Collections.singleton(createdParameter));
                sendNotificationsForBots(created);
            }
        }
    }

    @Override
    public void notifyPaidOrder(Order order) {
        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.ORDER_IS_PAID);
        userNotification.setUser(order.getUser());
        UserNotification notification = userNotificationRepository.save(userNotification);
        sendNotificationsForBots(notification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyCourierItineraryFormed(Order order) {
        Set<NotificationParameter> parameters = new HashSet<>();
        parameters.add(NotificationParameter.builder().key("date")
                .value(order.getDeliverFrom().format(DateTimeFormatter.ofPattern("dd-MM"))).build());
        parameters.add(NotificationParameter.builder().key("startTime")
                .value(order.getDeliverFrom().format(DateTimeFormatter.ofPattern("hh:mm"))).build());
        parameters.add(NotificationParameter.builder().key("endTime")
                .value(order.getDeliverTo().format(DateTimeFormatter.ofPattern("hh:mm"))).build());
        parameters.add(NotificationParameter.builder().key("phoneNumber")
                .value("+380638175035, +380931038987").build());
        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.COURIER_ITINERARY_FORMED);
        userNotification.setUser(order.getUser());
        UserNotification created = userNotificationRepository.save(userNotification);
        parameters.forEach(parameter -> parameter.setUserNotification(created));
        List<NotificationParameter> notificationParameters = notificationParameterRepository.saveAll(parameters);
        created.setParameters(new HashSet<>(notificationParameters));
        sendNotificationsForBots(created);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyHalfPaidPackage(Order order) {
        UserNotification userNotification = new UserNotification();
        Set<NotificationParameter> parameters = new HashSet<>();

        Long paidAmount = order.getPayment().stream()
                .filter(x -> !x.getPaymentStatus().equals(PaymentStatus.PAYMENT_REFUNDED))
                .map(Payment::getAmount).reduce(0L, Long::sum);

        List<Bag> bags = bagRepository.findBagByOrderId(order.getId());
        Map<Integer, Integer> amountOfBagsOrdered = order.getAmountOfBagsOrdered();

        Integer price = bags.stream().map(bag -> amountOfBagsOrdered.get(bag.getId()) * bag.getPrice())
                .reduce(0, Integer::sum);

        long amountToPay = price - paidAmount;

        parameters.add(NotificationParameter.builder().key("amountToPay")
                .value(String.format("%.2f", (double) amountToPay)).build());
        parameters.add(NotificationParameter.builder().key("orderNumber")
                .value(order.getId().toString()).build());

        userNotification.setNotificationType(NotificationType.UNPAID_PACKAGE);
        userNotification.setUser(order.getUser());

        UserNotification created = userNotificationRepository.save(userNotification);
        parameters.forEach(parameter -> parameter.setUserNotification(created));
        List<NotificationParameter> notificationParameters = notificationParameterRepository.saveAll(parameters);
        created.setParameters(new HashSet<>(notificationParameters));
        sendNotificationsForBots(created);
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

        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.ACCRUED_BONUSES_TO_ACCOUNT);
        userNotification.setUser(order.getUser());
        UserNotification created = userNotificationRepository.save(userNotification);
        parameters.forEach(parameter -> parameter.setUserNotification(created));
        List<NotificationParameter> notificationParameters = notificationParameterRepository.saveAll(parameters);
        created.setParameters(new HashSet<>(notificationParameters));
        sendNotificationsForBots(created);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyAddViolation(Order order) {
        UserNotification userNotification = new UserNotification();
        Set<NotificationParameter> parameters = new HashSet<>();
        violationRepository.findByOrderId(order.getId())
                .ifPresent(value -> parameters.add(NotificationParameter.builder()
                        .key("violationDescription")
                        .value(value.getDescription()).build()));
        userNotification.setNotificationType(NotificationType.VIOLATION_THE_RULES);
        userNotification.setUser(order.getUser());

        UserNotification created = userNotificationRepository.save(userNotification);
        parameters.forEach(parameter -> parameter.setUserNotification(created));
        List<NotificationParameter> notificationParameters = notificationParameterRepository.saveAll(parameters);
        created.setParameters(new HashSet<>(notificationParameters));
        sendNotificationsForBots(created);
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
                sendNotificationsForBots(notification);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NotificationDto> getAllNotificationsForUser(String userUuid, String language) {
        User user = userRepository.findByUuid(userUuid);
        List<UserNotification> notifications = userNotificationRepository.findAllByUser(user);
        List<NotificationDto> notificationDtos = new LinkedList<>();
        for (UserNotification notification : notifications) {
            notificationDtos.add(createNotificationDto(notification, language, notificationTemplateRepository));
        }
        return notificationDtos;
    }

    private void sendNotificationsForBots(UserNotification notification) {
        executor.execute(() -> {
            viberService.sendNotification(notification);
            telegramService.sendNotification(notification);
        });
    }

    public static NotificationDto createNotificationDto(UserNotification notification, String language,
                                                        NotificationTemplateRepository templateRepository) {
        NotificationTemplate template = templateRepository
                .findNotificationTemplateByNotificationTypeAndLanguageCode(
                        notification.getNotificationType(),
                        language)
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
}
