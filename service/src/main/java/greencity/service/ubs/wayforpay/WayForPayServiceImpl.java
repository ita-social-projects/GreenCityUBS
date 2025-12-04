package greencity.service.ubs.wayforpay;

import static greencity.constant.ErrorMessage.ORDER_NOT_FOUND_BY_ID;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_JOB_GROUP;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_JOB_KEY;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_SCHEDULE_EXCEPTION;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_TRIGGER_KEY;
import static greencity.constant.QuartzConstants.WAY_FOR_PAY_LINK_VALIDITY_SECONDS;
import greencity.client.WayForPayClient;
import greencity.constant.AppConstant;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.payment.PaymentCancellationWayForPayRequestDto;
import greencity.dto.payment.PaymentWayForPayRequestDto;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.exceptions.NotFoundException;
import greencity.properties.WayForPayProperties;
import greencity.repository.OrderRepository;
import greencity.scheduler.PaymentExpiryJob;
import greencity.util.EncryptionUtil;
import greencity.util.MoneyConverterUtil;
import greencity.util.OrderUtils;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WayForPayServiceImpl implements WayForPayService {
    private final OrderRepository orderRepository;
    private final MoneyConverterUtil moneyConverterUtil;
    private final EncryptionUtil encryptionUtil;
    private final WayForPayClient wayForPayClient;
    private final Scheduler quartzScheduler;
    private final WayForPayProperties wayForPayProperties;
    private static final String INVOICE_URL = "invoiceUrl";

    @Override
    @Transactional
    public PaymentSystemResponse processWayForPay(OrderResponseDto dto, Long orderId, long sumToPayInCoins) {
        PaymentWayForPayRequestDto requestDto = formPaymentRequestForWayForPay(orderId, sumToPayInCoins);
        String link = getLinkFromWayForPayCheckoutResponse(wayForPayClient.getCheckOutResponse(requestDto));
        schedulePaymentExpiryJob(
            orderId, dto.getPointsToUse(),
            dto.getCertificates(), WAY_FOR_PAY_LINK_VALIDITY_SECONDS, link);
        return getPaymentRequestDto(orderId, link);
    }

    @Override
    @Transactional
    public PaymentWayForPayRequestDto formPaymentRequestForWayForPay(Long orderId, long sumToPayInCoins) {
        Instant instant = Instant.now();
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND_BY_ID + orderId));
        PaymentWayForPayRequestDto paymentWayForPayRequestDto = PaymentWayForPayRequestDto.builder()
            .transactionType("CREATE_INVOICE")
            .merchantAccount(wayForPayProperties.getWayForPayLogin())
            .merchantDomainName(wayForPayProperties.getWayForPayMerchandDomainName())
            .apiVersion(1)
            .serviceUrl(wayForPayProperties.getWayForPayResultUrl())
            .orderReference(OrderUtils.generateEncodedOrderReference(order))
            .orderDate(instant.getEpochSecond())
            .amount(moneyConverterUtil.convertCoinsIntoBills(sumToPayInCoins).intValue())
            .currency("UAH")
            .orderTimeout(AppConstant.PAYMENT_VALIDITY_DURATION)
            .productName(order.getOrderBags().stream()
                .filter(bag -> bag.getAmount() != 0)
                .map(orderBag -> orderBag.getNameUk().trim())
                .flatMap(name -> Arrays.stream(name.split(",")))
                .toList())
            .productPrice(order.getOrderBags().stream()
                .filter(bag -> bag.getAmount() != 0)
                .map(product -> moneyConverterUtil.convertCoinsIntoBills(product.getPrice()).intValue())
                .toList())
            .productCount(order.getOrderBags().stream()
                .map(OrderBag::getAmount)
                .filter(amount -> amount != 0)
                .toList())
            .returnUrl(wayForPayProperties.getWayForPayReturnUrl())
            .build();

        paymentWayForPayRequestDto.setSignature(encryptionUtil
            .formRequestSignature(paymentWayForPayRequestDto, wayForPayProperties.getWayForPaySecret()));

        return paymentWayForPayRequestDto;
    }

    @Override
    public PaymentSystemResponse getPaymentRequestDto(Long orderId, String link) {
        return PaymentSystemResponse.builder()
            .orderId(orderId)
            .link(link)
            .build();
    }

    @Override
    public String getLinkFromWayForPayCheckoutResponse(String wayForPayResponse) {
        JSONObject json = new JSONObject(wayForPayResponse);
        if (!json.has(INVOICE_URL) || json.isNull(INVOICE_URL)) {
            log.error(wayForPayResponse);
            throw new IllegalStateException(
                "WayForPay response does not contain invoiceUrl. Response: " + wayForPayResponse
            );
        }
        return json.getString(INVOICE_URL);
    }

    @Override
    public PaymentCancellationWayForPayRequestDto formPaymentCancellationRequestForWayForPay(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND_BY_ID + orderId));
        PaymentCancellationWayForPayRequestDto paymentCancellationWayForPayRequestDto =
            PaymentCancellationWayForPayRequestDto.builder()
                .transactionType("REMOVE_INVOICE")
                .apiVersion(1)
                .merchantAccount(wayForPayProperties.getWayForPayLogin())
                .orderReference(OrderUtils.generateEncodedOrderReference(order))
                .build();

        paymentCancellationWayForPayRequestDto.setSignature(
            encryptionUtil.formRemoveInvoiceSignature(paymentCancellationWayForPayRequestDto,
                wayForPayProperties.getWayForPaySecret()));

        return paymentCancellationWayForPayRequestDto;
    }

    @Override
    public String getResultFromWayForPayCancellationResponse(String wayForPayResponse) {
        JSONObject json = new JSONObject(wayForPayResponse);
        return json.getString("reason");
    }

    @Override
    public void schedulePaymentExpiryJob(
        Long orderId, int pointsUsed, Set<String> certificateCodes, Long expirySeconds, String paymentLink) {
        if (certificateCodes == null) {
            certificateCodes = new HashSet<>();
        }

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("orderId", orderId);
        jobDataMap.put("pointsUsed", pointsUsed);
        jobDataMap.put("certificateCodes", certificateCodes);

        JobDetail job = JobBuilder.newJob(PaymentExpiryJob.class)
            .withIdentity(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP)
            .usingJobData(jobDataMap)
            .build();

        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity(PAYMENT_EXPIRY_TRIGGER_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP)
            .startAt(Date.from(Instant.now().plus(expirySeconds, ChronoUnit.SECONDS)))
            .build();

        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND_BY_ID + orderId));
            quartzScheduler.scheduleJob(job, trigger);
            order.setPaymentLink(paymentLink);
            order.setPaymentLinkExpiry(LocalDateTime.now().plusSeconds(expirySeconds));
            orderRepository.save(order);
        } catch (SchedulerException exception) {
            throw new IllegalStateException(PAYMENT_EXPIRY_SCHEDULE_EXCEPTION);
        }
    }
}
