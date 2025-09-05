package greencity.service.ubs.wayforpay;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

public interface WayForPayRedirectService {
    String redirectUser(Map<String, String> formParams);
}
