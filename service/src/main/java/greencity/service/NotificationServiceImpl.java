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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.stream.Collectors.toMap;


@Service
@Transactional
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserNotificationRepository userNotificationRepository;

    @Autowired
    private BagRepository bagRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ViolationRepository violationRepository;

    @Autowired
    private NotificationParameterRepository notificationParameterRepository;

    @Override
    public void notifyUnpaidOrders() {
        for (Order order : orderRepository.findAllByOrderPaymentStatus(OrderPaymentStatus.UNPAID)) {
            Optional<UserNotification> lastNotification = userNotificationRepository
                    .findLastNotificationByNotificationTypeAndOrderNumber(NotificationType.UNPAID_ORDER.toString(),
                            order.getId().toString());
            if ((lastNotification.isEmpty()
                    || lastNotification.get().getNotificationTime().isBefore(LocalDateTime.now().minusWeeks(1)))
                    && order.getOrderDate().isAfter(LocalDateTime.now().minusMonths(1))) {
                UserNotification userNotification = new UserNotification();
                if (lastNotification.isPresent()) {
                    UserNotification oldNotification = lastNotification.get();
                    userNotification.setUser(oldNotification.getUser());
                } else {
                    userNotification.setUser(order.getUser());
                }

                userNotification.setNotificationType(NotificationType.UNPAID_ORDER);
                UserNotification created = userNotificationRepository.save(userNotification);
                NotificationParameter notificationParameter = new NotificationParameter("orderNumber", order.getId()
                        .toString());
                notificationParameter.setUserNotification(created);
                notificationParameterRepository.save(notificationParameter);
            }
        }
    }

    @Override
    public void notifyPaidOrder(Order order) {
        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.ORDER_IS_PAID);
        userNotification.setUser(order.getUser());
        userNotificationRepository.save(userNotification);
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
        notificationParameterRepository.saveAll(parameters);
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
                .value(String.format("%.2f", (double)amountToPay)).build());
        parameters.add(NotificationParameter.builder().key("orderNumber")
                .value(order.getId().toString()).build());

        userNotification.setNotificationType(NotificationType.UNPAID_PACKAGE);
        userNotification.setUser(order.getUser());

        UserNotification created = userNotificationRepository.save(userNotification);
        parameters.forEach(parameter -> parameter.setUserNotification(created));
        notificationParameterRepository.saveAll(parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyAllHalfPaidPackages() {
        for (Order order: orderRepository.findAllByOrderPaymentStatus(OrderPaymentStatus.HALF_PAID)) {
            Optional<UserNotification> lastNotification = userNotificationRepository
                    .findLastNotificationByNotificationTypeAndOrderNumber(
                            NotificationType.UNPAID_PACKAGE.toString(),
                            order.getId().toString());
            if ((lastNotification.isEmpty()
                    || lastNotification.get().getNotificationTime().isBefore(LocalDateTime.now().minusWeeks(1)))
                    && order.getOrderDate().isAfter(LocalDateTime.now().minusMonths(1))) {
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
        notificationParameterRepository.saveAll(parameters);
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
        notificationParameterRepository.saveAll(parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyInactiveAccounts() {
        List<User> users = userRepository
                .getAllInactiveUsers(LocalDate.now().minusYears(1), LocalDate.now().minusMonths(2));
        log.info("Found {} inactive users", users.size());
        for (User user : users) {
            Optional<UserNotification> lastNotification =
                    userNotificationRepository
                            .findTop1UserNotificationByUserAndNotificationTypeOrderByNotificationTimeDesc(user,
                            NotificationType.LETS_STAY_CONNECTED);
            if (lastNotification.isEmpty()
                    || lastNotification.get().getNotificationTime().isBefore(LocalDateTime.now().minusWeeks(1))) {
                UserNotification userNotification = new UserNotification();
                userNotification.setNotificationType(NotificationType.LETS_STAY_CONNECTED);
                userNotification.setUser(user);
                userNotificationRepository.save(userNotification);
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
            NotificationTemplate template = notificationTemplateRepository
                    .findNotificationTemplateByNotificationTypeAndLanguageCode(
                            notification.getNotificationType(),
                            language
                    ).orElseThrow(() -> new NotFoundException("Template not found"));
            String templateBody = template.getBody();
            Map<String, String> valuesMap = notification.getParameters().stream()
                    .collect(toMap(NotificationParameter::getKey, NotificationParameter::getValue));

            StringSubstitutor sub = new StringSubstitutor(valuesMap);
            String resultBody = sub.replace(templateBody);

            notificationDtos.add(NotificationDto.builder().title(template.getTitle())
                    .body(resultBody).build());
        }
        return notificationDtos;
    }
}
