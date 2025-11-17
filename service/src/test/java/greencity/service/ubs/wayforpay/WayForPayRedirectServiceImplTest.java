package greencity.service.ubs.wayforpay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import greencity.constant.AppConstant;
import greencity.enums.PaymentStatus;
import greencity.exceptions.payment.InvalidPaymentResponseException;
import greencity.exceptions.payment.PaymentNotFoundException;
import greencity.properties.WayForPayProperties;
import greencity.repository.PaymentRepository;
import greencity.util.OrderUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WayForPayRedirectServiceImplTest {
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private WayForPayProperties wayForPayProperties;

    @InjectMocks
    private WayForPayRedirectServiceImpl service;

    private Map<String, String> formParams = new HashMap<>();

    @BeforeEach
    void setUp() {
        formParams.put("orderReference", "MTlfMl80MA");
    }

    @Test
    void redirectUser_shouldReturnCorrectUrl_whenPaymentApproved() {
        formParams.put("transactionStatus", "Approved");

        when(wayForPayProperties.getConfirmPageUrl()).thenReturn("http://localhost:4200/#/ubs/confirm");
        when(paymentRepository.getPaymentStatusByOrderIdAndPaymentId(anyLong(), anyLong()))
            .thenReturn(Optional.of(PaymentStatus.PAID));

        try (var mocked = mockStatic(OrderUtils.class)) {
            mocked.when(() -> OrderUtils.getIdByOrderReference(anyString(), eq(AppConstant.ORDER_ID_INDEX)))
                .thenReturn(19L);
            mocked.when(() -> OrderUtils.getIdByOrderReference(anyString(), eq(AppConstant.PAYMENT_ID_INDEX)))
                .thenReturn(0L);

            String redirectUrl = service.redirectUser(formParams);

            assertTrue(redirectUrl.startsWith("http://localhost:4200/#/ubs/confirm"));
            assertTrue(redirectUrl.contains("orderId=19"));
            assertTrue(redirectUrl.contains("status=paid"));
        }
    }

    @Test
    void redirectUser_shouldReturnUnpaid_whenTransactionDeclined() {
        formParams.put("transactionStatus", "Declined");

        when(wayForPayProperties.getConfirmPageUrl()).thenReturn("http://localhost:4200/#/ubs/confirm");
        when(paymentRepository.getPaymentStatusByOrderIdAndPaymentId(anyLong(), anyLong()))
            .thenReturn(Optional.of(PaymentStatus.PAID));

        try (var mocked = mockStatic(OrderUtils.class)) {
            mocked.when(() -> OrderUtils.decodeOrderReference("MTlfMl80MA"))
                .thenReturn("19_2_0");

            String redirectUrl = service.redirectUser(formParams);

            assertTrue(redirectUrl.contains("status=unpaid"));
        }
    }

    @Test
    void redirectUser_shouldReturnUnpaid_whenPaymentUnpaid() {
        formParams.put("transactionStatus", "Approved");

        when(wayForPayProperties.getConfirmPageUrl()).thenReturn("http://localhost:4200/#/ubs/confirm");
        when(paymentRepository.getPaymentStatusByOrderIdAndPaymentId(anyLong(), anyLong()))
            .thenReturn(Optional.of(PaymentStatus.UNPAID));

        try (var mocked = mockStatic(OrderUtils.class)) {
            mocked.when(() -> OrderUtils.decodeOrderReference("MTlfMl80MA"))
                .thenReturn("19_2_0");

            String redirectUrl = service.redirectUser(formParams);

            assertTrue(redirectUrl.contains("status=unpaid"));
        }
    }

    @Test
    void redirectUser_shouldThrowException_whenMissingParams() {
        Map<String, String> form = new HashMap<>();
        InvalidPaymentResponseException ex = assertThrows(InvalidPaymentResponseException.class,
            () -> service.redirectUser(form));

        assertEquals("Invalid payment response", ex.getMessage());
    }

    @Test
    void redirectUser_shouldThrowException_whenOnlyOrderReferencePresent() {
        InvalidPaymentResponseException ex = assertThrows(InvalidPaymentResponseException.class,
            () -> service.redirectUser(formParams));

        assertEquals("Invalid payment response", ex.getMessage());
    }

    @Test
    void redirectUser_shouldThrowException_whenPaymentNotFound() {
        formParams.put("transactionStatus", "Approved");
        when(paymentRepository.getPaymentStatusByOrderIdAndPaymentId(anyLong(), anyLong()))
            .thenReturn(Optional.empty());

        try (var mocked = mockStatic(OrderUtils.class)) {
            mocked.when(() -> OrderUtils.decodeOrderReference("MTlfMl80MA"))
                .thenReturn("19_2_0");

            PaymentNotFoundException ex = assertThrows(PaymentNotFoundException.class,
                () -> service.redirectUser(formParams));

            assertEquals("No payment found", ex.getMessage());
        }
    }
}