package greencity.properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import greencity.constant.ErrorMessage;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class WayForPayPropertiesTest {

    @Mock
    private Environment environment;

    @InjectMocks
    private WayForPayProperties wayForPayProperties;

    private LogCaptor logCaptor;

    @BeforeEach
    void setUp() {
        logCaptor = LogCaptor.forClass(WayForPayProperties.class);
        logCaptor.clearLogs();
    }

    @Test
    void getWayForPayLogin_shouldReturnValue_whenPropertyExists() {
        when(environment.getProperty("greencity.wayforpay.login")).thenReturn("loginValue");

        String result = wayForPayProperties.getWayForPayLogin();

        assertEquals("loginValue", result);
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void getWayForPayLogin_shouldLogError_whenPropertyEmpty() {
        when(environment.getProperty("greencity.wayforpay.login")).thenReturn("");
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> wayForPayProperties.getWayForPayLogin());

        assertEquals(ErrorMessage.WAYFORPAY_LOGIN_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.WAYFORPAY_LOGIN_NOT_FOUND));
    }

    @Test
    void getWayForPaySecret_shouldReturnValue_whenPropertyExists() {
        when(environment.getProperty("greencity.wayforpay.secret")).thenReturn("secretValue");

        String result = wayForPayProperties.getWayForPaySecret();

        assertEquals("secretValue", result);
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void getWayForPaySecret_shouldLogError_whenPropertyEmpty() {
        when(environment.getProperty("greencity.wayforpay.secret")).thenReturn("");
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> wayForPayProperties.getWayForPaySecret());

        assertEquals(ErrorMessage.WAYFORPAY_SECRET_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.WAYFORPAY_SECRET_NOT_FOUND));
    }

    @Test
    void getWayForPayMerchandDomainName_shouldReturnValue_whenPropertyExists() {
        when(environment.getProperty("greencity.wayforpay.merchant.domain.name")).thenReturn("merchantName");

        String result = wayForPayProperties.getWayForPayMerchandDomainName();

        assertEquals("merchantName", result);
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void getWayForPayMerchandDomainName_shouldLogError_whenPropertyEmpty() {
        when(environment.getProperty("greencity.wayforpay.merchant.domain.name")).thenReturn("");
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> wayForPayProperties.getWayForPayMerchandDomainName());

        assertEquals(ErrorMessage.WAYFORPAY_MERCHANT_DOMAIN_NAME_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.WAYFORPAY_MERCHANT_DOMAIN_NAME_NOT_FOUND));
    }

    @Test
    void getWayForPayResultUrl_shouldReturnValue_whenPropertyExists() {
        when(environment.getProperty("greencity.redirect.result-way-for-pay-url")).thenReturn("resultUrl");

        String result = wayForPayProperties.getWayForPayResultUrl();

        assertEquals("resultUrl", result);
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void getWayForPayResultUrl_shouldLogError_whenPropertyEmpty() {
        when(environment.getProperty("greencity.redirect.result-way-for-pay-url")).thenReturn("");
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> wayForPayProperties.getWayForPayResultUrl());

        assertEquals(ErrorMessage.WAYFORPAY_RESULT_URL_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.WAYFORPAY_RESULT_URL_NOT_FOUND));
    }

    @Test
    void getWayForPayReturnUrl_shouldReturnValue_whenPropertyExists() {
        when(environment.getProperty("greencity.redirect.green-city-client")).thenReturn("returnUrl");

        String result = wayForPayProperties.getWayForPayReturnUrl();

        assertEquals("returnUrl", result);
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void getWayForPayReturnUrl_shouldLogError_whenPropertyEmpty() {
        when(environment.getProperty("greencity.redirect.green-city-client")).thenReturn("");
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> wayForPayProperties.getWayForPayReturnUrl());

        assertEquals(ErrorMessage.WAYFORPAY_RETURN_URL_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.WAYFORPAY_RETURN_URL_NOT_FOUND));
    }

    @Test
    void getConfirmPageUrl_shouldReturnValue_whenPropertyExists() {
        when(environment.getProperty("redirect.confirm-page")).thenReturn("confirmUrl");

        String result = wayForPayProperties.getConfirmPageUrl();

        assertEquals("confirmUrl", result);
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void getConfirmPageUrl_shouldLogError_whenPropertyEmpty() {
        when(environment.getProperty("redirect.confirm-page")).thenReturn("");
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> wayForPayProperties.getConfirmPageUrl());

        assertEquals(ErrorMessage.WAYFORPAY_CONFIRM_PAGE_URL_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.WAYFORPAY_CONFIRM_PAGE_URL_NOT_FOUND));
    }

    @Test
    void validateProperties_shouldLogInfo_whenAllPropertiesValid() {
        when(environment.getProperty("greencity.wayforpay.login"))
            .thenReturn("login");
        when(environment.getProperty("greencity.wayforpay.secret"))
            .thenReturn("secret");
        when(environment.getProperty("greencity.wayforpay.merchant.domain.name"))
            .thenReturn("domain-name");
        when(environment.getProperty("greencity.redirect.result-way-for-pay-url"))
            .thenReturn("result-url");
        when(environment.getProperty("greencity.redirect.green-city-client"))
            .thenReturn("return-url");
        when(environment.getProperty("redirect.confirm-page"))
            .thenReturn("confirm-page");

        wayForPayProperties.validateProperties();

        assertTrue(logCaptor.getInfoLogs()
            .contains("All WayForPay properties validated successfully."));
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void validateProperties_shouldThrowException_whenAnyPropertyInvalid() {
        when(environment.getProperty("greencity.wayforpay.login"))
            .thenReturn("login");
        when(environment.getProperty("greencity.wayforpay.secret"))
            .thenReturn("secret");
        when(environment.getProperty("greencity.wayforpay.merchant.domain.name"))
            .thenReturn("domain-name");
        when(environment.getProperty("greencity.redirect.result-way-for-pay-url"))
            .thenReturn("result-url");
        when(environment.getProperty("greencity.redirect.green-city-client"))
            .thenReturn("return-url");
        when(environment.getProperty("redirect.confirm-page"))
            .thenReturn("");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> wayForPayProperties.validateProperties());

        assertEquals(ErrorMessage.WAYFORPAY_CONFIRM_PAGE_URL_NOT_FOUND, exception.getMessage());
        assertTrue(logCaptor.getErrorLogs().contains(ErrorMessage.WAYFORPAY_CONFIRM_PAGE_URL_NOT_FOUND));
    }
}