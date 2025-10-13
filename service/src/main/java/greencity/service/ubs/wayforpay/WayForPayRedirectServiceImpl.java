package greencity.service.ubs.wayforpay;

import greencity.config.GreenCityRedirectionConfigProp;
import greencity.constant.AppConstant;
import greencity.enums.PaymentStatus;
import greencity.exceptions.payment.InvalidPaymentResponseException;
import greencity.exceptions.payment.PaymentNotFoundException;
import greencity.repository.PaymentRepository;
import greencity.util.OrderUtils;
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
    public String redirectUser(Map<String, String> formParams) {
        String orderReference = formParams.get("orderReference");
        String transactionStatus = formParams.get("transactionStatus");
        validateParams(orderReference, transactionStatus);

        Long orderId = OrderUtils.getIdByOrderReference(orderReference, AppConstant.ORDER_ID_INDEX);
        Long paymentId = OrderUtils.getIdByOrderReference(orderReference, AppConstant.PAYMENT_ID_INDEX);

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
}
