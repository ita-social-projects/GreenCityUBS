package greencity.service.ubs.wayforpay;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

public interface WayForPayRedirectService {
    void redirectUser(Map<String, String> formParams, HttpServletResponse response);
}
