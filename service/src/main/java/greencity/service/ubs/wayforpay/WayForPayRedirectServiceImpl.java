package greencity.service.ubs.wayforpay;

import com.google.api.client.util.Value;
import greencity.config.GreenCityRedirectionConfigProp;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.PaymentStatus;
import greencity.repository.OrderRepository;
import greencity.repository.PaymentRepository;
import greencity.util.OrderUtils;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WayForPayRedirectServiceImpl implements WayForPayRedirectService {
    private static final String APPROVED_STATUS = "Approved";
    private final PaymentRepository paymentRepository;
    private final GreenCityRedirectionConfigProp redirectProp;

    @Override
    public void redirectUser(Map<String, String> formParams,
        HttpServletResponse response) {
        String orderReference = formParams.get("orderReference");
        String transactionStatus = formParams.get("transactionStatus");
        validateParams(orderReference, transactionStatus);

        Long orderId = getOrderIdByOrderReference(orderReference, 0);
        Long paymentId = getOrderIdByOrderReference(orderReference, 2);

        PaymentStatus paymentStatus = paymentRepository
            .getPaymentStatusByOrderIdAndPaymentId(orderId, paymentId)
            .orElseThrow(() -> new IllegalStateException("No payment found"));

        log.info("Received orderPaymentStatus: {}", paymentStatus);

        String redirectUrl = buildRedirectUrl(orderId, paymentStatus, transactionStatus);
        log.info("Redirect URL: {}", redirectUrl);

        doRedirect(response, redirectUrl);
    }

    private void doRedirect(HttpServletResponse response, String redirectUrl) {
        try {
            response.sendRedirect(redirectUrl);
        } catch (IOException e) {
            log.error("Failed to redirect user to {}, fallback to /", redirectUrl, e);
            fallbackRedirect(response);
        }
    }

    private String buildRedirectUrl(Long orderId, PaymentStatus paymentStatus, String transactionStatus) {
        return redirectProp.getConfirmPage()
            + "?orderId=" + orderId
            + "&status=" + finalStatus(paymentStatus, transactionStatus);
    }

    private void validateParams(String orderReference, String transactionStatus) {
        if (orderReference == null || transactionStatus == null) {
            log.error("Missing required WayForPay params");
            throw new IllegalArgumentException("Invalid payment response");
        }
    }

    private void fallbackRedirect(HttpServletResponse response) {
        try {
            response.sendRedirect("/");
        } catch (IOException ex) {
            log.error("Even fallback redirect failed", ex);
        }
    }

    private String finalStatus(PaymentStatus paymentStatus, String transactionStatus) {
        return paymentStatus == PaymentStatus.PAID
            && APPROVED_STATUS.equalsIgnoreCase(transactionStatus)
                ? PaymentStatus.PAID.name().toLowerCase()
                : PaymentStatus.UNPAID.name().toLowerCase();
    }

    private Long getOrderIdByOrderReference(String orderReference, int index) {
        String decodeOrderReference = OrderUtils.decodeOrderReference(orderReference);
        String[] parts = decodeOrderReference.split("_");
        return Long.parseLong(parts[index]);
    }
}
