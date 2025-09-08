package greencity.service.ubs.wayforpay;

import static greencity.constant.ErrorMessage.PAYMENT_VALIDATION_ERROR;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.constant.AppConstant;
import greencity.constant.OrderHistory;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.payment.PaymentResponseWayForPay;
import greencity.entity.notifications.UserNotification;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.enums.NotificationType;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.exceptions.BadRequestException;
import greencity.repository.NotificationParameterRepository;
import greencity.repository.OrderRepository;
import greencity.repository.PaymentRepository;
import greencity.repository.UserNotificationRepository;
import greencity.service.ubs.EventService;
import greencity.util.EncryptionUtil;
import greencity.util.OrderUtils;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WayForPayResultServiceimpl implements WayForPayResultService {
    private final ObjectMapper objectMapper;
    private final EncryptionUtil encryptionUtil;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final NotificationParameterRepository notificationParameterRepository;
    private final EventService eventService;

    @Value("${greencity.wayforpay.secret}")
    private String wayForPaySecret;

    @Override
    @Transactional
    public PaymentResponseWayForPay convertMapIntoPaymentResponseDto(Map<String, String> formParams) {
        if (formParams == null || formParams.isEmpty()) {
            return buildErrorResponse("No form params received");
        }
        log.debug("Received {} form param(s) from WayForPay", formParams.size());

        String jsonKey = formParams.keySet().iterator().next();
        log.debug("Extracted JSON from param key: {}", jsonKey);

        PaymentResponseDto dto;
        try {
            dto = getPaymentResponseDto(jsonKey);
        } catch (JsonProcessingException e) {
            return buildErrorResponse("Invalid JSON format");
        }

        if (isInvalidSignature(dto)) {
            return buildErrorResponse("Invalid signature");
        }

        log.info("Valid signature for orderReference={}", dto.getOrderReference());
        return validatePayment(dto);
    }

    private PaymentResponseWayForPay buildErrorResponse(String message) {
        log.error("Payment processing error: {}", message);
        return PaymentResponseWayForPay.builder()
            .status("ERROR")
            .orderReference(null)
            .time(LocalDateTime.now().toString())
            .signature(null)
            .build();
    }

    private PaymentResponseDto getPaymentResponseDto(String jsonKey) throws JsonProcessingException {
        PaymentResponseDto dto = objectMapper.readValue(jsonKey, PaymentResponseDto.class);
        log.info("Processing payment: orderReference={}, status={}",
            dto.getOrderReference(), dto.getTransactionStatus());
        return dto;
    }

    private boolean isInvalidSignature(PaymentResponseDto dto) {
        String calculatedSignature = encryptionUtil.generateResponseSignature(dto, wayForPaySecret);
        if (!calculatedSignature.equals(dto.getMerchantSignature())) {
            log.error("Invalid signature for orderReference={}", dto.getOrderReference());
            return true;
        }
        return false;
    }

    private PaymentResponseWayForPay validatePayment(PaymentResponseDto response) {
        String decodedOrderReference = OrderUtils.decodeOrderReference(response.getOrderReference());
        Payment orderPayment = mapPayment(response, decodedOrderReference);
        String[] ids = decodedOrderReference.split("_");
        Order order = orderRepository.findById(Long.valueOf(ids[0]))
            .orElseThrow(() -> new BadRequestException(PAYMENT_VALIDATION_ERROR));
        checkResponseStatusFailure(response, orderPayment, order);
        checkOrderStatusApproved(response, orderPayment, order, decodedOrderReference);
        PaymentResponseWayForPay accept = PaymentResponseWayForPay.builder()
            .orderReference(response.getOrderReference())
            .status("accept")
            .time(response.getCreatedDate()).build();
        accept.setSignature(encryptionUtil.formResponseSignature(accept, wayForPaySecret));
        return accept;
    }

    private Payment mapPayment(PaymentResponseDto response, String decodedOrderReference) {
        if (response.getFee() == null) {
            response.setFee("0");
        }
        return Payment.builder()
            .id(Long.valueOf(decodedOrderReference
                .substring(decodedOrderReference.lastIndexOf("_") + 1)))
            .currency(response.getCurrency())
            .amount(Long.parseLong(response.getAmount()) * AppConstant.CURRENCY_CONVERSION_RATE)
            .orderStatus(OrderStatus.FORMED)
            .senderCellPhone(response.getPhone())
            .maskedCard(response.getCardPan())
            .cardType(response.getCardType())
            .orderTime(response.getCreatedDate())
            .settlementDate(parseSettlementDate(""))
            .fee(0L)
            .paymentSystem(response.getPaymentSystem())
            .senderEmail(response.getEmail())
            .paymentStatus(PaymentStatus.UNPAID)
            .build();
    }

    private String parseSettlementDate(String settlementDate) {
        return settlementDate.isEmpty()
            ? LocalDate.now().toString()
            : LocalDate.parse(settlementDate, DateTimeFormatter.ofPattern("dd.MM.yyyy")).toString();
    }

    private void removePaymentLinkForOrder(Order order) {
        List<UserNotification> userNotification = userNotificationRepository
            .findAllUserNotificationByOrderAndNotificationType(order, NotificationType.UNPAID_ORDER);
        if (!userNotification.isEmpty()) {
            userNotification.stream()
                .map(notification -> notificationParameterRepository
                    .findNotificationParameterByUserNotificationAndKey(notification, AppConstant.PAY_BUTTON))
                .forEach(parameter -> parameter.ifPresent(notificationParameterRepository::delete));
        }
    }

    private void checkResponseStatusFailure(PaymentResponseDto dto, Payment orderPayment, Order order) {
        if (dto.getTransactionStatus().equals(AppConstant.FAILED_STATUS)) {
            orderPayment.setPaymentStatus(PaymentStatus.UNPAID);
            order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
            paymentRepository.save(orderPayment);
            orderRepository.save(order);
            log.info("Payment failed: orderId={}, transactionStatus={}, paymentStatus={}",
                order.getId(), dto.getTransactionStatus(), orderPayment.getPaymentStatus());
        }
    }

    private void checkOrderStatusApproved(PaymentResponseDto dto,
                                            Payment orderPayment,
                                            Order order,
                                            String decodedOrderReference) {
        if (dto.getTransactionStatus().equals(AppConstant.APPROVED_STATUS)) {
            orderPayment.setPaymentId(decodedOrderReference.split("_")[1]);
            orderPayment.setPaymentStatus(PaymentStatus.PAID);
            order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
            orderPayment.setOrder(order);
            removePaymentLinkForOrder(order);
            paymentRepository.save(orderPayment);
            orderRepository.save(order);
            eventService.save(OrderHistory.ORDER_PAID_UK, OrderHistory.SYSTEM_UK, order);
            eventService.save(OrderHistory.ADD_PAYMENT_SYSTEM_UK + orderPayment.getPaymentId(),
                OrderHistory.SYSTEM_UK, order);
            log.info("Payment approved: orderId={}, status={}",
                order.getId(), orderPayment.getPaymentStatus());
        } else {
            log.info("Payment not approved: orderId={}, transactionStatus={}",
                order.getId(), dto.getTransactionStatus());
        }
    }
}
