package greencity.service;

import greencity.dto.NotificationDto;
import greencity.entity.enums.NotificationType;
import greencity.entity.notifications.NotificationTemplate;
import greencity.entity.notifications.UserNotification;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.NotFoundException;
import greencity.repository.NotificationTemplateRepository;
import greencity.repository.UBSuserRepository;
import greencity.repository.UserNotificationRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;

    @Autowired
    private UBSuserRepository ubSuserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserNotificationRepository userNotificationRepository;

    public List<NotificationTemplate> getNotificationTemplates() {
        return notificationTemplateRepository.findAll();
    }

    @Override
    public void notifyUnpaidOrders() {
        List<UBSuser> ubSusers = ubSuserRepository.getAllUBSusersWhoHaveNotPaid(LocalDate.now().minusDays(3));
        for (UBSuser ubSuser : ubSusers) {
            UserNotification userNotification = new UserNotification();
            userNotification.setNotificationType(NotificationType.UNPAID_ORDER);
            userNotification.setUser(ubSuser.getUser());
            userNotificationRepository.save(userNotification);
        }
    }

    @Override
    public void notifyPaidOrder(Order order) {
        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.ORDER_IS_PAID);
        userNotification.setUser(order.getUser());
        userNotificationRepository.save(userNotification);
    }

    @Override
    public void notifyCourierItineraryFormed(Order order) {
        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.COURIER_ITINERARY_FORMED);
        userNotification.setUser(order.getUser());
        userNotificationRepository.save(userNotification);
    }

    @Override
    public void notifyHalfPaidPackage(Order order) {
        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.UNPAID_PACKAGE);
        userNotification.setUser(order.getUser());
        userNotificationRepository.save(userNotification);
    }

    @Override
    public void notifyBonuses(Order order) {
        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.ACCRUED_BONUSES_TO_ACCOUNT);
        userNotification.setUser(order.getUser());
        userNotificationRepository.save(userNotification);
    }

    @Override
    public void notifyAddViolation(Order order) {
        UserNotification userNotification = new UserNotification();
        userNotification.setNotificationType(NotificationType.VIOLATION_THE_RULES);
        userNotification.setUser(order.getUser());
        userNotificationRepository.save(userNotification);
    }

    @Override
    public void notifyInactiveAccounts() {
        List<UBSuser> ubSusers = ubSuserRepository.getAllInactiveUsers(LocalDate.now().minusMonths(2));
        for (UBSuser ubSuser : ubSusers) {
            UserNotification userNotification = new UserNotification();
            userNotification.setNotificationType(NotificationType.LETS_STAY_CONNECTED);
            userNotification.setUser(ubSuser.getUser());
            userNotificationRepository.save(userNotification);
        }
    }

    @Override
    public List<NotificationDto> getAllNotificationsForUser(String userUuid, String language) {
        User user = userRepository.findByUuid(userUuid);
        List<UserNotification> notifications = userNotificationRepository.findAllByUser(user);
        List<NotificationDto> notificationDtos = new LinkedList<>();
        for (UserNotification notification:notifications) {
            NotificationTemplate template = notificationTemplateRepository
                    .findNotificationTemplateByNotificationTypeAndLanguageCode(
                    notification.getNotificationType(), language)
                    .orElseThrow(()->new NotFoundException("Template not found"));

            notificationDtos.add(NotificationDto.builder().title(template.getTitle())
                    .body(template.getBody()).build());
        }
        return notificationDtos;
    }
}
