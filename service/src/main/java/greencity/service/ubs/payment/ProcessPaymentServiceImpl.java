package greencity.service.ubs.payment;

import static greencity.constant.ErrorMessage.ORDER_ALREADY_PAID;
import static greencity.constant.ErrorMessage.ORDER_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.ORDER_STATUS_AND_PAYMENT_CONDITION_FAILED;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST;
import static greencity.util.OrderUtils.getLastPayment;
import greencity.client.WayForPayClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.payment.PaymentWayForPayRequestDto;
import greencity.dto.user.PersonalDataDto;
import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.OrderAddress;
import greencity.entity.user.ubs.UBSuser;
import greencity.enums.BonusReason;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.address.AddressNotWithinLocationAreaException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.repository.OrderRepository;
import greencity.repository.UBSUserRepository;
import greencity.repository.UserRepository;
import greencity.service.phone.UAPhoneNumberUtil;
import greencity.service.ubs.AddressService;
import greencity.service.ubs.EventService;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.calculator.PaymentCalculatorService;
import greencity.service.ubs.order.OrderService;
import greencity.service.ubs.wayforpay.WayForPayService;
import greencity.util.OrderUtils;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessPaymentServiceImpl implements ProcessPaymentService {
    private final UserRepository userRepository;
    private final UBSUserRepository ubsUserRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final PaymentCalculatorService paymentCalculatorService;
    private final EventService eventService;
    private final WayForPayService wayForPayService;
    private final AddressService addressService;
    private final OrderService orderService;
    private final PaymentStrategyFactory paymentStrategyFactory;
    private final WayForPayClient wayForPayClient;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public PaymentSystemResponse processNewOrder(OrderResponseDto dto, String uuid) {
        validateOrderRequestAddress(dto);

        adjustPaymentDetails(dto);

        Order order = modelMapper.map(dto, Order.class);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.FORMED);
        order.setCounterOrderPaymentId(0L);

        User currentUser = userRepository.findByUuid(uuid);

        OrderAddress orderAddress = addressService.formAndSaveOrderAddress(
            dto.getAddressId(), dto.getLocationId(), currentUser);

        UBSuser userData = formAndSaveUbsUser(
            dto.getPersonalData(), null, orderAddress, currentUser);
        order = orderService.formAndSaveOrderRequest(dto, order, currentUser, userData);
        long sumToPayInCoins = getLastPayment(order).getAmount();

        formAndSaveUser(currentUser, dto.getPointsToUse(), order);

        saveOrderEvent(OrderHistory.ORDER_FORMED_UK, OrderHistory.CLIENT_UK, order);

        PaymentSystemResponse paymentSystemResponse =
            processPaymentResponse(dto, order, sumToPayInCoins);

        notificationService.notifyCreatedOrder(order);

        return paymentSystemResponse;
    }

    @Override
    @Transactional
    public PaymentSystemResponse processExistingOrder(OrderResponseDto dto, String uuid, Long orderId) {
        validateOrderRequestAddress(dto);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND_BY_ID + orderId));

        User currentUser = userRepository.findByUuid(uuid);
        checkIsOrderOfCurrentUser(currentUser, order);

        if (order.getOrderStatus() != OrderStatus.FORMED
            || order.getOrderPaymentStatus() != OrderPaymentStatus.UNPAID) {
            throw new BadRequestException(ORDER_STATUS_AND_PAYMENT_CONDITION_FAILED);
        }

        adjustPaymentDetails(dto);

        order.setPointsToUse(dto.getPointsToUse());
        order.setAdditionalOrders(dto.getAdditionalOrders());
        order.setComment(dto.getOrderComment());

        OrderAddress orderAddress = addressService.getOrUpdateOrderAddress(
            order.getUbsUser().getOrderAddress(), dto.getAddressId(), dto.getLocationId(), currentUser);

        UBSuser userData = formAndSaveUbsUser(
            dto.getPersonalData(), order.getUbsUser().getId(), orderAddress, currentUser);

        order = orderService.formAndSaveOrderRequest(dto, order, currentUser, userData);
        long sumToPayInCoins = getLastPayment(order).getAmount();

        formAndSaveUser(currentUser, dto.getPointsToUse(), order);

        saveOrderEvent(OrderHistory.ORDER_STATUS_UPDATED_UK, OrderHistory.CLIENT_UK, order);

        PaymentSystemResponse paymentSystemResponse =
            processPaymentResponse(dto, order, sumToPayInCoins);

        if (order.getOrderPaymentStatus() == OrderPaymentStatus.UNPAID) {
            notificationService.notifyUnpaidOrderPermanently(order, sumToPayInCoins, paymentSystemResponse);
        }

        return paymentSystemResponse;
    }

    @Override
    @Transactional
    public PaymentSystemResponse processOrder(String userUuid, OrderWayForPayClientDto dto) {
        Order order = orderRepository.findById(dto.getOrderId())
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + dto.getOrderId()));
        checkOrderIsPaid(order.getOrderPaymentStatus());
        User currentUser = userRepository.findUserByUuid(userUuid)
            .orElseThrow(() -> new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST + userUuid));
        checkForNullCounter(order);
        long sumToPayInCoins = paymentCalculatorService.calculateSumToPay(dto, order, currentUser);

        orderService.transferUserPointsToOrder(order, dto.getPointsToUse());
        paymentVerification(sumToPayInCoins, order);

        if (sumToPayInCoins <= 0) {
            return wayForPayService.getPaymentRequestDto(order, null);
        } else {
            String link = formedLink(order, sumToPayInCoins);
            return wayForPayService.getPaymentRequestDto(order, link);
        }
    }

    @Override
    @Transactional
    public String formedLink(Order order, long sumToPayInCoins) {
        Order increment = incrementCounter(order);
        PaymentWayForPayRequestDto paymentWayForPayRequestDto =
            wayForPayService.formPaymentRequestForWayForPay(increment.getId(), sumToPayInCoins);
        paymentWayForPayRequestDto
            .setOrderReference(OrderUtils.generateEncodedOrderReference(increment.getId(), order));
        return wayForPayService.getLinkFromWayForPayCheckoutResponse(
            wayForPayClient.getCheckOutResponse(paymentWayForPayRequestDto));
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
        PersonalDataDto dto, Long id, OrderAddress orderAddress, User currentUser) {
        UBSuser userData = modelMapper.map(dto, UBSuser.class);
        userData.setId(id);
        userData.setUser(currentUser);
        userData.setPhoneNumber(
            UAPhoneNumberUtil.getE164PhoneNumberFormat(userData.getPhoneNumber()));
        userData.setOrderAddress(orderAddress);
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
        eventService.save(eventName, author, order);
        log.info("Saved event: eventName={}, author={}, orderId={}", eventName, author, order.getId());
    }

    private PaymentSystemResponse processPaymentResponse(OrderResponseDto dto, Order order, long sumToPayInCoins) {
        if (dto.isShouldBePaid()) {
            return paymentStrategyFactory.getPaymentStrategy(dto.getPaymentSystem())
                .processPayment(order, sumToPayInCoins);
        } else {
            return wayForPayService.getPaymentRequestDto(order, "");
        }
    }

    private void checkIsOrderOfCurrentUser(User user, Order order) {
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(ErrorMessage.ORDER_DOES_NOT_BELONG_TO_USER);
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
            eventService.save(OrderHistory.ORDER_CONFIRMED_UK, OrderHistory.SYSTEM_UK, order);
        }
    }

    private Order incrementCounter(Order order) {
        order.setCounterOrderPaymentId(order.getCounterOrderPaymentId() + 1);
        orderRepository.save(order);
        return order;
    }
}
