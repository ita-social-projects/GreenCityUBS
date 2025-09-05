package greencity.service.ubs.wayforpay;

import greencity.config.GreenCityRedirectionConfigProp;
import greencity.enums.PaymentStatus;
import greencity.exceptions.DecodeOrderReferenceException;
import greencity.exceptions.payment.InvalidPaymentResponseException;
import greencity.exceptions.payment.PaymentNotFoundException;
import greencity.repository.PaymentRepository;
import greencity.util.OrderUtils;
import jakarta.servlet.http.HttpServletResponse;
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
    private static final int ORDER_ID_INDEX = 0;
    private static final int PAYMENT_ID_INDEX = 2;

    @Override
    public String redirectUser(Map<String, String> formParams,
        HttpServletResponse response) {
        String orderReference = formParams.get("orderReference");
        String transactionStatus = formParams.get("transactionStatus");
        validateParams(orderReference, transactionStatus);

        Long orderId = getOrderIdByOrderReference(orderReference, ORDER_ID_INDEX);
        Long paymentId = getOrderIdByOrderReference(orderReference, PAYMENT_ID_INDEX);

        PaymentStatus paymentStatus = paymentRepository
            .getPaymentStatusByOrderIdAndPaymentId(orderId, paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("No payment found"));

        log.info("Received orderPaymentStatus: {}", paymentStatus);

        String redirectUrl = buildRedirectUrl(orderId, paymentStatus, transactionStatus);
        log.info("Redirect URL: {}", redirectUrl);
        return redirectUrl;
    }

    private String buildRedirectUrl(Long orderId, PaymentStatus paymentStatus, String transactionStatus) {
        return Optional.ofNullable(redirectProp.getConfirmPage())
            .filter(s -> !s.isBlank())
            .orElseThrow(() -> new IllegalStateException("Confirm page URL is not configured"))
            + "?orderId=" + orderId
            + "&status=" + finalStatus(paymentStatus, transactionStatus);
    }

    private void validateParams(String orderReference, String transactionStatus) {
        if (orderReference == null || orderReference.isBlank()
            || transactionStatus == null || transactionStatus.isBlank()) {
            log.error("Missing required WayForPay params: orderReference or transactionStatus");
            throw new InvalidPaymentResponseException("Invalid payment response");
        }
    }

    private String finalStatus(PaymentStatus paymentStatus, String transactionStatus) {
        return paymentStatus == PaymentStatus.PAID
            && APPROVED_STATUS.equalsIgnoreCase(transactionStatus)
                ? PaymentStatus.PAID.name().toLowerCase()
                : PaymentStatus.UNPAID.name().toLowerCase();
    }

    private Long getOrderIdByOrderReference(String orderReference, int index) {
        try {
            String decoded = OrderUtils.decodeOrderReference(orderReference);
            String[] parts = decoded.split("_");
            if (index >= parts.length) {
                throw new InvalidPaymentResponseException("Invalid payment response");
            }
            return Long.parseLong(parts[index]);
        } catch (DecodeOrderReferenceException | NumberFormatException ex) {
            log.error("Invalid orderReference format: {}", orderReference, ex);
            throw new InvalidPaymentResponseException("Invalid payment response");
        }
    }
}
