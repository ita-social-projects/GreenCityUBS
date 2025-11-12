package greencity.service.ubs.wayforpay;

import static greencity.constant.ErrorMessage.PAYMENT_VALIDATION_ERROR;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.constant.AppConstant;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.payment.PaymentResponseWayForPay;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.exceptions.BadRequestException;
import greencity.properties.WayForPayProperties;
import greencity.repository.OrderRepository;
import greencity.service.ubs.payment.PaymentStatusHandlerService;
import greencity.util.EncryptionUtil;
import greencity.util.OrderUtils;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WayForPayResultServiceImpl implements WayForPayResultService {
    private final ObjectMapper objectMapper;
    private final EncryptionUtil encryptionUtil;
    private final OrderRepository orderRepository;
    private final PaymentStatusHandlerService paymentStatusHandlerService;
    private final WayForPayProperties wayForPayProperties;

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
        String calculatedSignature =
            encryptionUtil.generateResponseSignature(dto, wayForPayProperties.getWayForPaySecret());
        if (!calculatedSignature.equals(dto.getMerchantSignature())) {
            log.error("Invalid signature for orderReference={}", dto.getOrderReference());
            return true;
        }
        return false;
    }

    private PaymentResponseWayForPay validatePayment(PaymentResponseDto response) {
        String decodedOrderReference = OrderUtils.decodeOrderReference(response.getOrderReference());
        Payment orderPayment = mapPayment(response, decodedOrderReference);
        long orderId = OrderUtils.getIdByOrderReference(
            response.getOrderReference(), AppConstant.ORDER_ID_INDEX);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BadRequestException(PAYMENT_VALIDATION_ERROR));

        paymentStatusHandlerService
            .checkResponseStatusFailure(orderPayment.getId(), order.getId(), response.getTransactionStatus());
        paymentStatusHandlerService.checkOrderStatusApproved(orderPayment.getId(), order.getId(),
            decodedOrderReference, response.getTransactionStatus());

        return getPaymentResponseWayForPay(response);
    }

    private PaymentResponseWayForPay getPaymentResponseWayForPay(PaymentResponseDto response) {
        PaymentResponseWayForPay accept = PaymentResponseWayForPay.builder()
            .orderReference(response.getOrderReference())
            .status("accept")
            .time(response.getCreatedDate()).build();
        accept.setSignature(encryptionUtil.formResponseSignature(accept, wayForPayProperties.getWayForPaySecret()));
        return accept;
    }

    private Payment mapPayment(PaymentResponseDto response, String decodedOrderReference) {
        if (response.getFee() == null) {
            response.setFee("0");
        }
        return Payment.builder()
            .id(Long.valueOf(decodedOrderReference
                .substring(decodedOrderReference.lastIndexOf("_")
                    + AppConstant.COUNTER_ORDER_PAYMENT_ID_INDEX)))
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
}
