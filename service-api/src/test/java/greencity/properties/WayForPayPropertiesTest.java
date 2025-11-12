package greencity.properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        assertEquals("", wayForPayProperties.getWayForPayLogin());
        assertTrue(logCaptor.getErrorLogs().contains("WayForPayLogin property is empty"));

        logCaptor.clearLogs();

        when(environment.getProperty("greencity.wayforpay.login")).thenReturn(null);
        assertNull(wayForPayProperties.getWayForPayLogin());
        assertTrue(logCaptor.getErrorLogs().contains("WayForPayLogin property is empty"));
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
        assertEquals("", wayForPayProperties.getWayForPaySecret());
        assertTrue(logCaptor.getErrorLogs().contains("WayForPaySecret property is empty"));

        logCaptor.clearLogs();

        when(environment.getProperty("greencity.wayforpay.secret")).thenReturn(null);
        assertNull(wayForPayProperties.getWayForPaySecret());
        assertTrue(logCaptor.getErrorLogs().contains("WayForPaySecret property is empty"));
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
        assertEquals("", wayForPayProperties.getWayForPayMerchandDomainName());
        assertTrue(logCaptor.getErrorLogs().contains("MerchantDomainName property is empty"));

        logCaptor.clearLogs();

        when(environment.getProperty("greencity.wayforpay.merchant.domain.name")).thenReturn(null);
        assertNull(wayForPayProperties.getWayForPayMerchandDomainName());
        assertTrue(logCaptor.getErrorLogs().contains("MerchantDomainName property is empty"));
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
        assertEquals("", wayForPayProperties.getWayForPayResultUrl());
        assertTrue(logCaptor.getErrorLogs().contains("WayForPayResultUrl property is empty"));

        logCaptor.clearLogs();

        when(environment.getProperty("greencity.redirect.result-way-for-pay-url")).thenReturn(null);
        assertNull(wayForPayProperties.getWayForPayResultUrl());
        assertTrue(logCaptor.getErrorLogs().contains("WayForPayResultUrl property is empty"));
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
        assertEquals("", wayForPayProperties.getWayForPayReturnUrl());
        assertTrue(logCaptor.getErrorLogs().contains("WayForPayReturnUrl property is empty"));

        logCaptor.clearLogs();

        when(environment.getProperty("greencity.redirect.green-city-client")).thenReturn(null);
        assertNull(wayForPayProperties.getWayForPayReturnUrl());
        assertTrue(logCaptor.getErrorLogs().contains("WayForPayReturnUrl property is empty"));
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
        assertEquals("", wayForPayProperties.getConfirmPageUrl());
        assertTrue(logCaptor.getErrorLogs().contains("ConfirmPageUrl property is empty"));

        logCaptor.clearLogs();

        when(environment.getProperty("redirect.confirm-page")).thenReturn(null);
        assertNull(wayForPayProperties.getConfirmPageUrl());
        assertTrue(logCaptor.getErrorLogs().contains("ConfirmPageUrl property is empty"));
    }
}