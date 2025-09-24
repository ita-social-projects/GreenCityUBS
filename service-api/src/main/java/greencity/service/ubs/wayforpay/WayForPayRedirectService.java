package greencity.service.ubs.wayforpay;

import java.util.Map;

public interface WayForPayRedirectService {
    String redirectUser(Map<String, String> formParams);
}
