package greencity.properties;

import greencity.constant.ErrorMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Сlass for retrieving configuration values from the runtime environment. Used
 * to access dynamic properties. Provides a flexible alternative to @Value,
 * always getting the latest values without having to restart the application or
 * use /actuator/refresh.
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class WayForPayProperties {
    private final Environment environment;

    @PostConstruct
    public void validateProperties() {
        getWayForPayLogin();
        getWayForPaySecret();
        getWayForPayMerchandDomainName();
        getWayForPayResultUrl();
        getWayForPayReturnUrl();
        getConfirmPageUrl();
        log.info("All WayForPay properties validated successfully.");
    }

    public String getWayForPayLogin() {
        String wayForPayLogin = environment.getProperty("greencity.wayforpay.login");
        if (!StringUtils.hasText(wayForPayLogin)) {
            log.error(ErrorMessage.WAYFORPAY_LOGIN_NOT_FOUND);
            throw new IllegalStateException(ErrorMessage.WAYFORPAY_LOGIN_NOT_FOUND);
        }
        return wayForPayLogin;
    }

    public String getWayForPaySecret() {
        String wayForPaySecret = environment.getProperty("greencity.wayforpay.secret");
        if (!StringUtils.hasText(wayForPaySecret)) {
            log.error(ErrorMessage.WAYFORPAY_SECRET_NOT_FOUND);
            throw new IllegalStateException(ErrorMessage.WAYFORPAY_SECRET_NOT_FOUND);
        }
        return wayForPaySecret;
    }

    public String getWayForPayMerchandDomainName() {
        String merchantDomainName = environment.getProperty("greencity.wayforpay.merchant.domain.name");
        if (!StringUtils.hasText(merchantDomainName)) {
            log.error(ErrorMessage.WAYFORPAY_MERCHANT_DOMAIN_NAME_NOT_FOUND);
            throw new IllegalStateException(ErrorMessage.WAYFORPAY_MERCHANT_DOMAIN_NAME_NOT_FOUND);
        }
        return merchantDomainName;
    }

    public String getWayForPayResultUrl() {
        String wayForPayResultUrl = environment.getProperty("greencity.redirect.result-way-for-pay-url");
        if (!StringUtils.hasText(wayForPayResultUrl)) {
            log.error(ErrorMessage.WAYFORPAY_RESULT_URL_NOT_FOUND);
            throw new IllegalStateException(ErrorMessage.WAYFORPAY_RESULT_URL_NOT_FOUND);
        }
        return wayForPayResultUrl;
    }

    public String getWayForPayReturnUrl() {
        String wayForPayReturnUrl = environment.getProperty("greencity.redirect.green-city-client");
        if (!StringUtils.hasText(wayForPayReturnUrl)) {
            log.error(ErrorMessage.WAYFORPAY_RETURN_URL_NOT_FOUND);
            throw new IllegalStateException(ErrorMessage.WAYFORPAY_RETURN_URL_NOT_FOUND);
        }
        return wayForPayReturnUrl;
    }

    public String getConfirmPageUrl() {
        String confirmPageUrl = environment.getProperty("redirect.confirm-page");
        if (!StringUtils.hasText(confirmPageUrl)) {
            log.error(ErrorMessage.WAYFORPAY_CONFIRM_PAGE_URL_NOT_FOUND);
            throw new IllegalStateException(ErrorMessage.WAYFORPAY_CONFIRM_PAGE_URL_NOT_FOUND);
        }
        return confirmPageUrl;
    }
}
