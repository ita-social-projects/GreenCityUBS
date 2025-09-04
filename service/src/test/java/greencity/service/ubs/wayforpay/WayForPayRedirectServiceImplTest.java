package greencity.service.ubs.wayforpay;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import greencity.config.GreenCityRedirectionConfigProp;
import greencity.enums.PaymentStatus;
import greencity.exceptions.DecodeOrderReferenceException;
import greencity.exceptions.payment.InvalidPaymentResponseException;
import greencity.exceptions.payment.PaymentNotFoundException;
import greencity.repository.PaymentRepository;
import greencity.util.OrderUtils;
import jakarta.servlet.http.HttpServletResponse;
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
    private GreenCityRedirectionConfigProp redirectProp;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private WayForPayRedirectServiceImpl service;

    private Map<String, String> formParams;

    @BeforeEach
    void setUp() {
        formParams = new HashMap<>();
        formParams.put("orderReference", "MTlfMl80MA");
    }

    @Test
    void redirectUser_shouldReturnCorrectUrl_whenPaymentApproved() {
        formParams.put("transactionStatus", "Approved");

        when(redirectProp.getConfirmPage()).thenReturn("http://localhost:4200/#/ubs/confirm");
        when(paymentRepository.getPaymentStatusByOrderIdAndPaymentId(anyLong(), anyLong()))
            .thenReturn(Optional.of(PaymentStatus.PAID));

        try (var mocked = mockStatic(OrderUtils.class)) {
            mocked.when(() -> OrderUtils.decodeOrderReference("MTlfMl80MA"))
                .thenReturn("19_2_0");

            String redirectUrl = service.redirectUser(formParams, response);

            assertTrue(redirectUrl.startsWith("http://localhost:4200/#/ubs/confirm"));
            assertTrue(redirectUrl.contains("orderId=19"));
            assertTrue(redirectUrl.contains("status=paid"));
        }
    }

    @Test
    void redirectUser_shouldReturnUnpaid_whenTransactionDeclined() {
        formParams.put("transactionStatus", "Declined");

        when(redirectProp.getConfirmPage()).thenReturn("http://localhost:4200/#/ubs/confirm");
        when(paymentRepository.getPaymentStatusByOrderIdAndPaymentId(anyLong(), anyLong()))
            .thenReturn(Optional.of(PaymentStatus.PAID));

        try (var mocked = mockStatic(OrderUtils.class)) {
            mocked.when(() -> OrderUtils.decodeOrderReference("MTlfMl80MA"))
                .thenReturn("19_2_0");

            String redirectUrl = service.redirectUser(formParams, response);

            assertTrue(redirectUrl.contains("status=unpaid"));
        }
    }

    @Test
    void redirectUser_shouldReturnUnpaid_whenPaymentUnpaid() {
        formParams.put("transactionStatus", "Approved");

        when(redirectProp.getConfirmPage()).thenReturn("http://localhost:4200/#/ubs/confirm");
        when(paymentRepository.getPaymentStatusByOrderIdAndPaymentId(anyLong(), anyLong()))
            .thenReturn(Optional.of(PaymentStatus.UNPAID));

        try (var mocked = mockStatic(OrderUtils.class)) {
            mocked.when(() -> OrderUtils.decodeOrderReference("MTlfMl80MA"))
                .thenReturn("19_2_0");

            String redirectUrl = service.redirectUser(formParams, response);

            assertTrue(redirectUrl.contains("status=unpaid"));
        }
    }

    @Test
    void redirectUser_shouldThrowException_whenWrongDecodedOrderReference() {
        try (var mocked = mockStatic(OrderUtils.class)) {
            mocked.when(() -> OrderUtils.decodeOrderReference("MTlfMl80MA"))
                .thenReturn("1920");

            InvalidPaymentResponseException ex = assertThrows(InvalidPaymentResponseException.class,
                () -> service.redirectUser(formParams, response));

            assertEquals("Invalid payment response", ex.getMessage());
        }
    }

    @Test
    void redirectUser_shouldThrowInvalidPaymentResponseException_whenDecodeFails() {
        try (var mocked = mockStatic(OrderUtils.class)) {
            mocked.when(() -> OrderUtils.decodeOrderReference("MTlfMl80MA"))
                .thenThrow(new DecodeOrderReferenceException("Decode failed"));

            InvalidPaymentResponseException ex = assertThrows(
                InvalidPaymentResponseException.class,
                () -> service.redirectUser(formParams, response));
            assertEquals("Invalid payment response", ex.getMessage());
        }
    }

    @Test
    void redirectUser_shouldThrowInvalidPaymentResponseException_whenNumberFormatInvalid() {
        try (var mocked = mockStatic(OrderUtils.class)) {
            mocked.when(() -> OrderUtils.decodeOrderReference("MTlfMl80MA"))
                .thenReturn("two_two_two");

            InvalidPaymentResponseException ex = assertThrows(
                InvalidPaymentResponseException.class,
                () -> service.redirectUser(formParams, response));

            assertEquals("Invalid payment response", ex.getMessage());
        }
    }

    @Test
    void redirectUser_shouldThrowException_whenMissingParams() {
        InvalidPaymentResponseException ex = assertThrows(InvalidPaymentResponseException.class,
            () -> service.redirectUser(formParams, response));

        assertEquals("Invalid payment response", ex.getMessage());
    }

    @Test
    void redirectUser_shouldThrowException_whenOnlyOrderReferencePresent() {
        InvalidPaymentResponseException ex = assertThrows(InvalidPaymentResponseException.class,
            () -> service.redirectUser(formParams, response));

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
                () -> service.redirectUser(formParams, response));

            assertEquals("No payment found", ex.getMessage());
        }
    }
}