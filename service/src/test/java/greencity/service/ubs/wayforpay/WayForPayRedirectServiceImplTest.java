package greencity.service.ubs.wayforpay;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import greencity.config.GreenCityRedirectionConfigProp;
import greencity.enums.PaymentStatus;
import greencity.repository.PaymentRepository;
import greencity.util.OrderUtils;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @Test
    void redirectUser_shouldRedirectToCorrectUrl_whenPaymentApproved() throws Exception {
        // given
        Map<String, String> formParams = new HashMap<>();
        formParams.put("orderReference", "MTlfMl80MA");
        formParams.put("transactionStatus", "Approved");

        when(redirectProp.getConfirmPage()).thenReturn("http://localhost:4200/#/ubs/confirm");
        mockStatic(OrderUtils.class);
        when(OrderUtils.decodeOrderReference("MTlfMl80MA")).thenReturn("19_2_0");
        when(paymentRepository.getPaymentStatusByOrderIdAndPaymentId(
            any(Long.class), any(Long.class)))
            .thenReturn(Optional.of(PaymentStatus.PAID));

        service.redirectUser(formParams, response);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, times(1)).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertTrue(redirectUrl.contains("orderId="));
        assertTrue(redirectUrl.contains("status=paid"));
        assertTrue(redirectUrl.startsWith("http://localhost:4200/#/ubs/confirm"));
    }

    @Test
    void redirectUser_shouldRedirectToCorrectUrl_WithDeclinedStatus_whenPaymentApproved() throws Exception {
        Map<String, String> formParams = new HashMap<>();
        formParams.put("orderReference", "MTlfMl80MA");
        formParams.put("transactionStatus", "Declined");

        when(redirectProp.getConfirmPage()).thenReturn("http://localhost:4200/#/ubs/confirm");
        when(OrderUtils.decodeOrderReference("MTlfMl80MA")).thenReturn("19_2_0");
        when(paymentRepository.getPaymentStatusByOrderIdAndPaymentId(
            any(Long.class), any(Long.class)))
            .thenReturn(Optional.of(PaymentStatus.PAID));

        service.redirectUser(formParams, response);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, times(1)).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertTrue(redirectUrl.contains("orderId="));
        assertTrue(redirectUrl.contains("status=unpaid"));
        assertTrue(redirectUrl.startsWith("http://localhost:4200/#/ubs/confirm"));
    }

    @Test
    void redirectUser_shouldRedirectToCorrectUrl_WithUnpaidStatus_whenPaymentApproved() throws Exception {

        Map<String, String> formParams = new HashMap<>();
        formParams.put("orderReference", "MTlfMl80MA");
        formParams.put("transactionStatus", "Declined");

        when(redirectProp.getConfirmPage()).thenReturn("http://localhost:4200/#/ubs/confirm");
        when(OrderUtils.decodeOrderReference("MTlfMl80MA")).thenReturn("19_2_0");
        when(paymentRepository.getPaymentStatusByOrderIdAndPaymentId(
            any(Long.class), any(Long.class)))
            .thenReturn(Optional.of(PaymentStatus.UNPAID));

        service.redirectUser(formParams, response);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, times(1)).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertTrue(redirectUrl.contains("orderId="));
        assertTrue(redirectUrl.contains("status=unpaid"));
        assertTrue(redirectUrl.startsWith("http://localhost:4200/#/ubs/confirm"));
    }

    @Test
    void redirectUser_shouldThrowException_whenMissingParams() {
        Map<String, String> formParams = new HashMap<>();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.redirectUser(formParams, response));

        assertEquals("Invalid payment response", ex.getMessage());
        verifyNoInteractions(response);
    }

    @Test
    void redirectUser_shouldThrowException_whenMissingParamsOrderReference() {
        Map<String, String> formParams = new HashMap<>();
        formParams.put("orderReference", "MTlfMl80MA");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.redirectUser(formParams, response));

        assertEquals("Invalid payment response", ex.getMessage());
        verifyNoInteractions(response);
    }

    @Test
    void redirectUser_shouldFallback_whenIOExceptionOccurs() throws Exception {
        Map<String, String> formParams = new HashMap<>();
        formParams.put("orderReference", "MTlfMl80MA");
        formParams.put("transactionStatus", "Approved");

        when(redirectProp.getConfirmPage()).thenReturn("http://localhost:4200/#/ubs/confirm");
        when(paymentRepository.getPaymentStatusByOrderIdAndPaymentId(anyLong(), anyLong()))
            .thenReturn(Optional.of(PaymentStatus.PAID));

        doThrow(new java.io.IOException("IO Error")).when(response).sendRedirect(anyString());

        service.redirectUser(formParams, response);

        verify(response, times(2)).sendRedirect(anyString());
    }
}