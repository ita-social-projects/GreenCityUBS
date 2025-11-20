package greencity.service.ubs.payment;

import static greencity.constant.ErrorMessage.ORDER_ADDRESS_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.ORDER_ALREADY_PAID;
import static greencity.constant.ErrorMessage.ORDER_IN_ONGOING_PROCESSING;
import static greencity.constant.ErrorMessage.ORDER_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.ORDER_STATUS_AND_PAYMENT_CONDITION_FAILED;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.UNABLE_TO_CANCEL_PAYMENT_INVOICE;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST;
import static greencity.constant.QuartzConstants.NO_PAYMENT_ATTEMPT_FOR_ORDER;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_JOB_GROUP;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_JOB_KEY;
import static greencity.constant.QuartzConstants.QUARTZ_SCHEDULER_EXCEPTION;
import static greencity.constant.QuartzConstants.WAY_FOR_PAY_LINK_VALIDITY_SECONDS;
import static greencity.util.OrderUtils.getLastPayment;
import greencity.client.WayForPayClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.dto.order.OrderAddressDto;
import greencity.dto.order.OrderInfoDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.payment.PaymentCancellationWayForPayRequestDto;
import greencity.dto.payment.PaymentWayForPayRequestDto;
import greencity.dto.payment.PaymentWithStatusDto;
import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.UserPointDto;
import greencity.entity.order.Certificate;
import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.entity.user.User;
import greencity.entity.user.ubs.OrderAddress;
import greencity.entity.user.ubs.UBSuser;
import greencity.enums.*;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.address.AddressNotWithinLocationAreaException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.repository.CertificateRepository;
import greencity.repository.OrderAddressRepository;
import greencity.repository.OrderRepository;
import greencity.repository.UBSUserRepository;
import greencity.repository.UserRepository;
import greencity.service.phone.UAPhoneNumberUtil;
import greencity.service.ubs.AddressService;
import greencity.service.ubs.EventService;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.OrderBagService;
import greencity.service.ubs.PaymentUtil;
import greencity.service.ubs.calculator.PaymentCalculatorService;
import greencity.service.ubs.order.OrderService;
import greencity.service.ubs.wayforpay.WayForPayService;
import greencity.util.OrderUtils;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessPaymentServiceImpl implements ProcessPaymentService {
    private final UserRepository userRepository;
    private final UBSUserRepository ubsUserRepository;
    private final OrderRepository orderRepository;
    private final CertificateRepository certificateRepository;
    private final NotificationService notificationService;
    private final PaymentCalculatorService paymentCalculatorService;
    private final EventService eventService;
    private final WayForPayService wayForPayService;
    private final AddressService addressService;
    private final OrderService orderService;
    private final PaymentStrategyFactory paymentStrategyFactory;
    private final WayForPayClient wayForPayClient;
    private final ModelMapper modelMapper;
    private final Scheduler quartzScheduler;
    private final OrderBagService orderBagService;
    private final OrderAddressRepository orderAddressRepository;

    @Override
    @Transactional
    public PaymentSystemResponse processNewOrder(OrderResponseDto dto, String uuid) {
        validateOrderRequestAddress(dto);
        adjustPaymentDetails(dto);

        Order order = orderRepository.save(mapOrder(dto));
        User currentUser = userRepository.findByUuid(uuid);
        UBSuser userData = createOrUpdateUbsUser(dto, currentUser, null);

        Long orderId = orderService.formAndSaveOrderRequest(dto, order.getId(), currentUser.getId(),
            userData.getId());
        order = getOrder(orderId);
        long sumToPayInCoins = getLastPayment(order).getAmount();

        formAndSaveUser(currentUser, dto.getPointsToUse(), order);
        saveOrderEvent(OrderHistory.ORDER_FORMED_UK, OrderHistory.CLIENT_UK, order);

        PaymentSystemResponse paymentSystemResponse =
            processPaymentResponse(dto, order, sumToPayInCoins);

        notificationService.notifyCreatedOrder(order.getId());

        return paymentSystemResponse;
    }

    @Override
    @Transactional
    public PaymentSystemResponse processExistingOrder(OrderResponseDto dto, String uuid, Long orderId) {
        validateOrderRequestAddress(dto);
        User currentUser = userRepository.findByUuid(uuid);
        Order order = getOrder(orderId);
        validateOrderPaymentProcessingStatus(order);

        validateExistingOrder(order, currentUser);
        adjustPaymentDetails(dto);
        updateOrderFromDto(dto, order);

        UBSuser userData = createOrUpdateUbsUser(dto, currentUser, order);

        orderId = orderService.formAndSaveOrderRequest(dto, order.getId(), currentUser.getId(),
            userData.getId());
        order = getOrder(orderId);
        long sumToPayInCoins = getLastPayment(order).getAmount();

        formAndSaveUser(currentUser, dto.getPointsToUse(), order);
        saveOrderEvent(OrderHistory.ORDER_STATUS_UPDATED_UK, OrderHistory.CLIENT_UK, order);

        PaymentSystemResponse paymentSystemResponse =
            processPaymentResponse(dto, order, sumToPayInCoins);

        if (order.getOrderPaymentStatus() == OrderPaymentStatus.UNPAID) {
            notificationService.notifyUnpaidOrderPermanently(order.getId(), sumToPayInCoins, paymentSystemResponse);
        }

        return paymentSystemResponse;
    }

    @Override
    @Transactional
    public PaymentSystemResponse processOrder(String userUuid, OrderWayForPayClientDto dto) {
        Order order = getOrder(dto.getOrderId());
        checkOrderIsPaid(order.getOrderPaymentStatus());
        validateOrderPaymentProcessingStatus(order);
        User currentUser = getUserByUuid(userUuid);
        checkForNullCounter(order);

        OrderInfoDto orderInfo = modelMapper.map(order, OrderInfoDto.class);
        orderInfo.setOrderPrice(PaymentUtil.getPriceDetails(
            orderInfo.getId(), orderRepository, orderBagService, certificateRepository)
            .getTotalSumAmount());
        UserPointDto userPoints = modelMapper.map(currentUser, UserPointDto.class);
        long sumToPayInCoins = paymentCalculatorService.calculateSumToPay(dto, orderInfo, userPoints);

        orderService.transferUserPointsToOrder(order.getId(), dto.getPointsToUse());
        paymentVerification(sumToPayInCoins, order);

        if (sumToPayInCoins <= 0) {
            return wayForPayService.getPaymentRequestDto(order.getId(), null);
        } else {
            String link = formedLink(order.getId(), sumToPayInCoins, dto);
            return wayForPayService.getPaymentRequestDto(order.getId(), link);
        }
    }

    @Override
    @Transactional
    public String formedLink(Long orderId) {
        Order order = getOrder(orderId);
        validateOrderPaymentProcessingStatus(order);
        incrementCounter(order);
        long paymentsForCurrentOrder = order.getPayment().stream().filter(payment -> payment.getPaymentStatus()
            .equals(PaymentStatus.PAID)).map(Payment::getAmount)
            .reduce(Long::sum)
            .orElse((long) 0);
        long sumToPayInCoins = order.getSumTotalAmountWithoutDiscounts() - paymentsForCurrentOrder;
        log.info("sumToPayInCoins for PDF: " + sumToPayInCoins);

        PaymentWayForPayRequestDto paymentWayForPayRequestDto =
            wayForPayService.formPaymentRequestForWayForPay(order.getId(), sumToPayInCoins);
        paymentWayForPayRequestDto
            .setOrderReference(OrderUtils.generateEncodedOrderReference(order));
        String link = wayForPayService
            .getLinkFromWayForPayCheckoutResponse(wayForPayClient.getCheckOutResponse(paymentWayForPayRequestDto));

        wayForPayService.schedulePaymentExpiryJob(order.getId(), 0, new HashSet<>(),
            WAY_FOR_PAY_LINK_VALIDITY_SECONDS, link);
        return link;
    }

    @Override
    public String formedLink(Long orderId, long sumToPayInCoins, OrderWayForPayClientDto dto) {
        Order order = getOrder(orderId);
        incrementCounter(order);
        PaymentWayForPayRequestDto paymentWayForPayRequestDto =
            wayForPayService.formPaymentRequestForWayForPay(order.getId(), sumToPayInCoins);
        paymentWayForPayRequestDto
            .setOrderReference(OrderUtils.generateEncodedOrderReference(order));
        String link = wayForPayService
            .getLinkFromWayForPayCheckoutResponse(wayForPayClient.getCheckOutResponse(paymentWayForPayRequestDto));
        wayForPayService.schedulePaymentExpiryJob(
            order.getId(), dto.getPointsToUse(),
            dto.getCertificates(), WAY_FOR_PAY_LINK_VALIDITY_SECONDS, link);
        return link;
    }

    @Transactional
    @Override
    public void expirePaymentAttempt(Long orderId, int pointsUsed, Set<String> certificateCodes) {
        Order order = unlockSpecifiedPointsAndCertificatesFromOrder(orderId, pointsUsed, certificateCodes);
        order.setPaymentLink("");
        order.setPaymentLinkExpiry(null);
        List<PaymentWithStatusDto> payments = order.getPayment().stream()
            .map(payment -> modelMapper.map(payment, PaymentWithStatusDto.class))
            .toList();
        Long paidAmount = paymentCalculatorService.countPaidAmount(payments);
        if (paidAmount > 0) {
            log.info("Order has paid amount: " + paidAmount);
            order.setOrderPaymentStatus(OrderPaymentStatus.HALF_PAID);
        } else {
            log.info("Order doesnt have paid amount: " + paidAmount);
            order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        }
        orderRepository.save(order);
    }

    @Transactional
    @Override
    public void cancelPaymentAttempt(String uuid, Long orderId) {
        if (!checkExistsOrderExpiryJob(orderId)) {
            throw new BadRequestException(NO_PAYMENT_ATTEMPT_FOR_ORDER);
        }

        User currentUser = userRepository.findByUuid(uuid);
        Order order = orderRepository.findById(orderId).orElseThrow(
            () -> new NotFoundException(ORDER_NOT_FOUND_BY_ID + orderId));
        checkIsOrderOfCurrentUser(currentUser, order);

        JobDataMap jobDataMap = getPaymentExpiryJobData(orderId);
        int pointsUsed = jobDataMap.getInt("pointsUsed");
        @SuppressWarnings("unchecked")
        HashSet<String> certificateCodes = (HashSet<String>) jobDataMap.get("certificateCodes");

        expirePaymentAttempt(orderId, pointsUsed, certificateCodes);
        orderService.cancelPaymentExpiryJob(orderId);
        handleWayForPayPaymentCancellation(order);
    }

    private void validateOrderPaymentProcessingStatus(Order order) {
        if (isOrderInActivePaymentAttempt(order)) {
            throw new BadRequestException(ORDER_IN_ONGOING_PROCESSING);
        }
    }

    private boolean isOrderInActivePaymentAttempt(Order order) {
        String paymentLink = order.getPaymentLink();
        if (paymentLink == null) {
            return false;
        }
        return !paymentLink.isBlank();
    }

    private UBSuser createOrUpdateUbsUser(OrderResponseDto dto, User currentUser, Order existingOrder) {
        OrderAddressDto orderAddress;

        if (existingOrder == null) {
            orderAddress = addressService.formAndSaveOrderAddress(
                dto.getAddressId(), dto.getLocationId(), currentUser.getId());
            return formAndSaveUbsUser(dto.getPersonalData(), null, orderAddress, currentUser);
        } else {
            orderAddress = modelMapper.map(existingOrder.getUbsUser().getOrderAddress(), OrderAddressDto.class);
            orderAddress = addressService.getOrUpdateOrderAddress(
                orderAddress, dto.getAddressId(), dto.getLocationId(), currentUser.getId());
            return formAndSaveUbsUser(dto.getPersonalData(), existingOrder.getUbsUser().getId(), orderAddress,
                currentUser);
        }
    }

    private Order mapOrder(OrderResponseDto dto) {
        Order order = modelMapper.map(dto, Order.class);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.FORMED);
        order.setCounterOrderPaymentId(0L);
        return order;
    }

    private void updateOrderFromDto(OrderResponseDto dto, Order order) {
        order.setPointsToUse(dto.getPointsToUse());
        order.setAdditionalOrders(dto.getAdditionalOrders());
        order.setComment(dto.getOrderComment());
    }

    private void validateExistingOrder(Order order, User currentUser) {
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ErrorMessage.ORDER_DOES_NOT_BELONG_TO_USER);
        }

        if (order.getOrderStatus() != OrderStatus.FORMED
            || order.getOrderPaymentStatus() != OrderPaymentStatus.UNPAID) {
            throw new BadRequestException(ORDER_STATUS_AND_PAYMENT_CONDITION_FAILED);
        }
    }

    private User getUserByUuid(String userUuid) {
        return userRepository.findUserByUuid(userUuid)
            .orElseThrow(() -> new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST + userUuid));
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
    }

    private void validateOrderRequestAddress(OrderResponseDto dto) {
        if (!addressService.checkIfAddressMatchLocationArea(dto.getLocationId(), dto.getAddressId())) {
            throw new AddressNotWithinLocationAreaException(AppConstant.ADDRESS_NOT_WITHIN_LOCATION_AREA_MESSAGE);
        }
    }

    private void adjustPaymentDetails(OrderResponseDto dto) {
        if (!dto.isShouldBePaid()) {
            dto.setCertificates(Collections.emptySet());
            dto.setPointsToUse(0);
        }
    }

    private UBSuser formAndSaveUbsUser(
        PersonalDataDto dto, Long id, OrderAddressDto orderAddress, User currentUser) {
        OrderAddress orderAddressToSave = orderAddressRepository.findById(orderAddress.getId())
            .orElseThrow(() -> new NotFoundException(ORDER_ADDRESS_NOT_FOUND_BY_ID + orderAddress.getId()));
        UBSuser userData = modelMapper.map(dto, UBSuser.class);
        userData.setId(id);
        userData.setUser(currentUser);
        userData.setPhoneNumber(
            UAPhoneNumberUtil.getE164PhoneNumberFormat(userData.getPhoneNumber()));
        userData.setOrderAddress(orderAddressToSave);
        userData = ubsUserRepository.save(userData);

        currentUser.getUbsUsers().add(userData);
        currentUser.setRecipientSurname(dto.getLastName());
        currentUser.setRecipientName(dto.getFirstName());
        currentUser.setRecipientPhone(dto.getPhoneNumber());
        userRepository.save(currentUser);

        return userData;
    }

    private void formAndSaveUser(User currentUser, int pointsToUse, Order order) {
        currentUser.getOrders().add(order);
        if (pointsToUse != 0) {
            currentUser.setCurrentPoints(currentUser.getCurrentPoints() - pointsToUse);
            currentUser.getChangeOfPointsList().add(ChangeOfPoints.builder()
                .amount(-pointsToUse)
                .date(order.getOrderDate())
                .user(currentUser)
                .order(order)
                .reason(BonusReason.DEBIT_PAYMENT)
                .build());
        }
        userRepository.save(currentUser);
    }

    private void saveOrderEvent(String eventName, String author, Order order) {
        eventService.save(eventName, author, order.getId());
        log.info("Saved event: eventName={}, author={}, orderId={}", eventName, author, order.getId());
    }

    private PaymentSystemResponse processPaymentResponse(OrderResponseDto dto, Order order, long sumToPayInCoins) {
        if (dto.isShouldBePaid()) {
            return paymentStrategyFactory.getPaymentStrategy(dto.getPaymentSystem())
                .processPayment(dto, order.getId(), sumToPayInCoins);
        } else {
            return wayForPayService.getPaymentRequestDto(order.getId(), "");
        }
    }

    private void checkOrderIsPaid(OrderPaymentStatus orderPaymentStatus) {
        if (OrderPaymentStatus.PAID.equals(orderPaymentStatus)) {
            throw new BadRequestException(ORDER_ALREADY_PAID);
        }
    }

    private void checkForNullCounter(Order order) {
        if (order.getCounterOrderPaymentId() == null) {
            order.setCounterOrderPaymentId(0L);
        }
    }

    private void paymentVerification(long sumToPayInCoins, Order order) {
        if (sumToPayInCoins <= 0) {
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            order.setOrderStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            eventService.save(OrderHistory.ORDER_CONFIRMED_UK, OrderHistory.SYSTEM_UK, order.getId());
        }
    }

    private void incrementCounter(Order order) {
        order.setCounterOrderPaymentId(order.getCounterOrderPaymentId() + 1);
        orderRepository.save(order);
    }

    private Order unlockSpecifiedPointsAndCertificatesFromOrder(
        Long orderId, int pointsToUse, Set<String> certificateCodes) {
        Order order = orderRepository.findById(orderId).orElseThrow(
            () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));

        if (!certificateCodes.isEmpty()) {
            unlockSpecifiedCertificatesFromOrder(orderId, certificateCodes);
        }

        if (pointsToUse > 0) {
            return unlockSpecifiedPointsFromOrder(order, pointsToUse);
        }
        return order;
    }

    private void unlockSpecifiedCertificatesFromOrder(Long orderId, Set<String> certificateCodes) {
        Set<Certificate> certificates = certificateRepository.findAllByCodesAndOrderId(
            certificateCodes.stream().toList(), orderId);
        certificates.forEach((certificate -> certificate
            .setOrder(null)
            .setDateOfUse(null)
            .setCertificateStatus(CertificateStatus.ACTIVE)
            .setPoints(certificate.getInitialPointsValue())));
        certificateRepository.saveAll(certificates);
    }

    private Order unlockSpecifiedPointsFromOrder(Order order, int pointsToUse) {
        User user = order.getUser();

        order.setPointsToUse(order.getPointsToUse() - pointsToUse);
        user.setCurrentPoints(user.getCurrentPoints() + pointsToUse);
        user.getChangeOfPointsList()
            .add(ChangeOfPoints.builder()
                .user(user)
                .amount(pointsToUse)
                .date(LocalDateTime.now())
                .order(order)
                .reason(BonusReason.RETURN_UNPAID_ORDER)
                .build());
        userRepository.save(user);
        return order;
    }

    private boolean checkExistsOrderExpiryJob(Long orderId) {
        JobKey jobKey = JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP);
        try {
            return quartzScheduler.checkExists(jobKey);
        } catch (SchedulerException exception) {
            throw new IllegalStateException(QUARTZ_SCHEDULER_EXCEPTION);
        }
    }

    private void checkIsOrderOfCurrentUser(User user, Order order) {
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(ErrorMessage.ORDER_DOES_NOT_BELONG_TO_USER);
        }
    }

    private JobDataMap getPaymentExpiryJobData(Long orderId) {
        JobKey jobKey = JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP);

        try {
            return quartzScheduler.getJobDetail(jobKey).getJobDataMap();
        } catch (SchedulerException e) {
            throw new IllegalStateException(QUARTZ_SCHEDULER_EXCEPTION);
        }
    }

    private void handleWayForPayPaymentCancellation(Order order) {
        PaymentCancellationWayForPayRequestDto requestDto = wayForPayService
            .formPaymentCancellationRequestForWayForPay(order.getId());
        String result = wayForPayService.getResultFromWayForPayCancellationResponse(
            wayForPayClient.getCancellationResponse(requestDto));

        if (!result.equals("Removed")) {
            throw new BadRequestException(UNABLE_TO_CANCEL_PAYMENT_INVOICE);
        }
    }
}
