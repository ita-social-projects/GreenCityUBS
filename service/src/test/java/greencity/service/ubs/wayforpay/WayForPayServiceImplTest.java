package greencity.service.ubs.wayforpay;

import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_SCHEDULE_EXCEPTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import greencity.ModelUtils;
import greencity.client.WayForPayClient;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.payment.PaymentCancellationWayForPayRequestDto;
import greencity.dto.payment.PaymentWayForPayRequestDto;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.exceptions.NotFoundException;
import greencity.repository.OrderRepository;
import greencity.util.EncryptionUtil;
import greencity.util.MoneyConverterUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WayForPayServiceImplTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private MoneyConverterUtil moneyConverterUtil;
    @Mock
    private EncryptionUtil encryptionUtil;
    @Mock
    private WayForPayClient wayForPayClient;
    @Mock
    private Scheduler quartzScheduler;

    @InjectMocks
    private WayForPayServiceImpl wayForPayService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(wayForPayService, "resultWayForPayUrl", "http://test-result");
        ReflectionTestUtils.setField(wayForPayService, "merchantAccount", "testAccount");
        ReflectionTestUtils.setField(wayForPayService, "wayForPaySecret", "secret");
        ReflectionTestUtils.setField(wayForPayService, "merchantDomainName", "testDomain");
        ReflectionTestUtils.setField(wayForPayService, "greenCityClientUrl", "http://green-client");
    }

    @Test
    void testProcessWayForPay_Success() {
        Order order = ModelUtils.getOrder();
        order.setId(1L);
        order.setOrderBags(List.of());
        OrderResponseDto orderResponseDto = ModelUtils.getOrderResponseDto();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(moneyConverterUtil.convertCoinsIntoBills(anyLong())).thenReturn(Double.valueOf(100));
        when(encryptionUtil.formRequestSignature(any(), anyString())).thenReturn("signed");
        when(wayForPayClient.getCheckOutResponse(any())).thenReturn("{\"invoiceUrl\":\"http://pay-link\"}");

        PaymentSystemResponse response = wayForPayService.processWayForPay(orderResponseDto, order.getId(), 500L);

        assertNotNull(response);
        assertEquals(1L, response.orderId());
        assertEquals("http://pay-link", response.link());
    }

    @Test
    void testFormPaymentRequestForWayForPay_Success() {
        long sumToPayInCoins = 1000L;
        Long orderId = 2L;
        Order order = ModelUtils.getOrder();
        order.setId(orderId);

        OrderBag bag1 = ModelUtils.getOrderBag();
        bag1.setNameUk("Пакет1");
        bag1.setAmount(2);
        bag1.setPrice(200L);

        OrderBag bag2 = ModelUtils.getOrderBag();
        bag2.setNameUk("Пакет2");
        bag2.setAmount(0);

        order.setOrderBags(List.of(bag1, bag2));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(moneyConverterUtil.convertCoinsIntoBills(200L)).thenReturn(Double.valueOf(2));
        when(moneyConverterUtil.convertCoinsIntoBills(1000L)).thenReturn(Double.valueOf(10));
        when(encryptionUtil.formRequestSignature(any(), anyString())).thenReturn("test-signature");

        PaymentWayForPayRequestDto dto = wayForPayService.formPaymentRequestForWayForPay(orderId, sumToPayInCoins);

        assertNotNull(dto);
        assertEquals("testAccount", dto.getMerchantAccount());
        assertEquals("testDomain", dto.getMerchantDomainName());
        assertEquals("http://test-result", dto.getServiceUrl());
        assertEquals("UAH", dto.getCurrency());
        assertEquals("test-signature", dto.getSignature());

        assertEquals(List.of("Пакет1"), dto.getProductName());
        assertEquals(List.of(2), dto.getProductCount());
    }

    @Test
    void testFormPaymentRequestForWayForPay_OrderNotFound() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> wayForPayService.formPaymentRequestForWayForPay(999L, 1000L));
    }

    @Test
    void testGetPaymentRequestDto() {
        Order order = ModelUtils.getOrder();
        order.setId(5L);

        PaymentSystemResponse response = wayForPayService.getPaymentRequestDto(order.getId(), "http://link");

        assertEquals(5L, response.orderId());
        assertEquals("http://link", response.link());
    }

    @Test
    void testGetLinkFromWayForPayCheckoutResponse() {
        String jsonResponse = "{\"invoiceUrl\":\"http://invoice-test\"}";

        String link = wayForPayService.getLinkFromWayForPayCheckoutResponse(jsonResponse);

        assertEquals("http://invoice-test", link);
    }

    @Test
    void formPaymentCancellationRequestForWayForPay_createsValidRequest() {
        Order order = ModelUtils.getOrder();
        String expectedSignature = "signed123";

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(encryptionUtil.formRemoveInvoiceSignature(any(), eq("secret")))
            .thenReturn(expectedSignature);

        PaymentCancellationWayForPayRequestDto result =
            wayForPayService.formPaymentCancellationRequestForWayForPay(order.getId());

        assertEquals("REMOVE_INVOICE", result.getTransactionType());
        assertEquals(1, result.getApiVersion());
        assertEquals("testAccount", result.getMerchantAccount());
        assertNotNull(result.getOrderReference());
        assertEquals(expectedSignature, result.getSignature());

        verify(encryptionUtil).formRemoveInvoiceSignature(result, "secret");
    }

    @Test
    void getResultFromWayForPayCancellationResponse_returnsReason() {
        String jsonResponse = "{\"reason\": \"Transaction cancelled successfully\", \"status\": \"ok\"}";

        String result = wayForPayService.getResultFromWayForPayCancellationResponse(jsonResponse);

        assertEquals("Transaction cancelled successfully", result);
    }

    @Test
    void schedulePaymentExpiryJob_schedulesJobSuccessfully() throws SchedulerException {
        Order order = new Order();
        order.setId(1L);
        int pointsUsed = 100;
        Set<String> certificates = Set.of("CERT123");
        Long expirySeconds = 60L;
        String paymentLink = "http://payment.com/123";

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        wayForPayService.schedulePaymentExpiryJob(order.getId(), pointsUsed, certificates, expirySeconds, paymentLink);

        verify(quartzScheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
        verify(orderRepository).save(order);

        assertEquals(paymentLink, order.getPaymentLink());
        assertNotNull(order.getPaymentLinkExpiry());
        assertTrue(order.getPaymentLinkExpiry().isAfter(LocalDateTime.now()));
    }

    @Test
    void schedulePaymentExpiryJob_whenSchedulerThrows_throwsIllegalStateException() throws SchedulerException {
        Order order = new Order();
        order.setId(1L);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        doThrow(new SchedulerException("fail")).when(quartzScheduler)
            .scheduleJob(any(JobDetail.class), any(Trigger.class));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> wayForPayService.schedulePaymentExpiryJob(1L, 0, null,
                30L, "http://x"));

        assertEquals(PAYMENT_EXPIRY_SCHEDULE_EXCEPTION, exception.getMessage());
    }
}