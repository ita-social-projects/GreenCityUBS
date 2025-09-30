package greencity.service.ubs.wayforpay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.constant.AppConstant;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.payment.PaymentResponseWayForPay;
import greencity.entity.order.Order;
import greencity.repository.OrderRepository;
import greencity.service.ubs.payment.PaymentStatusHandlerService;
import greencity.util.EncryptionUtil;
import greencity.util.OrderUtils;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WayForPayResultServiceImplTest {
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private EncryptionUtil encryptionUtil;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentStatusHandlerService paymentStatusHandlerService;

    @InjectMocks
    private WayForPayResultServiceImpl wayForPayResultService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(wayForPayResultService, "wayForPaySecret", "secret");
    }


    @Test
    void testConvertMapIntoPaymentResponseDto_emptyFormParams() {
        PaymentResponseWayForPay result = wayForPayResultService.convertMapIntoPaymentResponseDto(Collections.emptyMap());
        assertEquals("ERROR", result.getStatus());
        assertNull(result.getOrderReference());
    }

    @SneakyThrows
    @Test
    void testConvertMapIntoPaymentResponseDto_invalidJson() {
        Map<String, String> form = Map.of("invalid-json", "");

        when(objectMapper.readValue("invalid-json", PaymentResponseDto.class))
            .thenThrow(new JsonParseException(null, "fail"));

        PaymentResponseWayForPay result = wayForPayResultService.convertMapIntoPaymentResponseDto(form);

        assertEquals("ERROR", result.getStatus());
        assertNull(result.getOrderReference());
    }

    @SneakyThrows
    @Test
    void testConvertMapIntoPaymentResponseDto_invalidSignature() {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setOrderReference("ref123");
        dto.setMerchantSignature("bad-sign");

        //Because WayForPay send only to key all json
        String jsonKey = new ObjectMapper().writeValueAsString(dto);
        Map<String, String> form = Map.of(jsonKey, "");

        when(objectMapper.readValue(jsonKey, PaymentResponseDto.class)).thenReturn(dto);
        when(encryptionUtil.generateResponseSignature(dto, "secret")).thenReturn("good-sign");

        PaymentResponseWayForPay result = wayForPayResultService.convertMapIntoPaymentResponseDto(form);

        assertEquals("ERROR", result.getStatus());
        assertNull(result.getOrderReference());
    }

    @SneakyThrows
    @Test
    void testConvertMapIntoPaymentResponseDto_success() {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setOrderReference("MV8xXzE");
        dto.setMerchantSignature("sig");
        dto.setTransactionStatus("Approved");
        dto.setCreatedDate(LocalDateTime.now().toString());
        dto.setAmount("100");
        dto.setCurrency("UAH");

        Order order = ModelUtils.getOrder();
        order.setId(1L);

        //Because WayForPay send only to key all json
        String jsonKey = new ObjectMapper().writeValueAsString(dto);
        Map<String, String> form = Map.of(jsonKey, "");

        when(objectMapper.readValue(jsonKey, PaymentResponseDto.class)).thenReturn(dto);
        when(encryptionUtil.generateResponseSignature(dto, "secret")).thenReturn("sig");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        doNothing().when(paymentStatusHandlerService).checkResponseStatusFailure(any(), any(), any());
        doNothing().when(paymentStatusHandlerService).checkOrderStatusApproved(any(), any(), any(), any());
        when(encryptionUtil.formResponseSignature(any(), eq("secret"))).thenReturn("resp-sig");

        try (MockedStatic<OrderUtils> mocked = Mockito.mockStatic(OrderUtils.class)) {
            mocked.when(() -> OrderUtils.decodeOrderReference("MV8xXzE")).thenReturn("1_2_3");
            mocked.when(() -> OrderUtils.getIdByOrderReference("MV8xXzE", AppConstant.ORDER_ID_INDEX)).thenReturn(1L);
        }
        PaymentResponseWayForPay result = wayForPayResultService.convertMapIntoPaymentResponseDto(form);

        assertEquals("accept", result.getStatus());
        assertEquals("MV8xXzE", result.getOrderReference());
        assertEquals("resp-sig", result.getSignature());
    }
}