package greencity.properties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Сlass for retrieving configuration values from the runtime environment.
 * Used to access dynamic properties.
 * Provides a flexible alternative to @Value, always getting the latest values
 * without having to restart the application or use /actuator/refresh.
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class WayForPayProperties {
    private final Environment environment;

    public String getWayForPayLogin() {
        String wayForPayLogin = environment.getProperty("greencity.wayforpay.login");
        if (!StringUtils.hasText(wayForPayLogin)) {
            log.error("WayForPayLogin property is empty");
        }
        return wayForPayLogin;
    }

    public String getWayForPaySecret() {
        String wayForPaySecret = environment.getProperty("greencity.wayforpay.secret");
        if (!StringUtils.hasText(wayForPaySecret)) {
            log.error("WayForPaySecret property is empty");
        }
        return wayForPaySecret;
    }

    public String getWayForPayMerchandDomainName() {
        String merchantDomainName = environment.getProperty("greencity.wayforpay.merchant.domain.name");
        if (!StringUtils.hasText(merchantDomainName)) {
            log.error("MerchantDomainName property is empty");
        }
        return merchantDomainName;
    }

    public String getWayForPayResultUrl(){
        String wayForPayResultUrl = environment.getProperty("greencity.redirect.result-way-for-pay-url");
        if (!StringUtils.hasText(wayForPayResultUrl)) {
            log.error("WayForPayResultUrl property is empty");
        }
        return wayForPayResultUrl;
    }

    public String getWayForPayReturnUrl(){
        String wayForPayReturnUrl = environment.getProperty("greencity.redirect.green-city-client");
        if (!StringUtils.hasText(wayForPayReturnUrl)) {
            log.error("WayForPayReturnUrl property is empty");
        }
        return wayForPayReturnUrl;
    }

    public String getConfirmPageUrl(){
        String confirmPageUrl = environment.getProperty("redirect.confirm-page");
        if (!StringUtils.hasText(confirmPageUrl)) {
            log.error("ConfirmPageUrl property is empty");
        }
        return confirmPageUrl;
    }
}
