package greencity.util;

import greencity.entity.order.Order;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Component
@ToString
public class OrderUtils {
    private OrderUtils() {
    }

    /**
     * Generates an order ID string based on the given orderId and the {@link Order}
     * object, and encodes the resulting string using Base64. The generated string
     * is in the format: orderId_counterOrderPaymentId_paymentId
     *
     * @param orderId The unique identifier of the order.
     * @param order   The {@link Order} object containing details such as payment
     *                and counterOrderPaymentId.
     * @return A Base64-encoded string representing the generated order ID.
     */
    public static String generateEncodedOrderReference(Long orderId, Order order) {
        int lastNumber = order.getPayment().size() - 1;
        String rawOrderId = String.format("%s_%s_%s", orderId,
            (order.getCounterOrderPaymentId() == null) ? 1 : order.getCounterOrderPaymentId(),
            order.getPayment().get(lastNumber).getId());

        return Base64.getEncoder().encodeToString(rawOrderId.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes a Base64-encoded order ID string back into its original format. The
     * original string should have the format:
     * orderId_counterOrderPaymentId_paymentId.
     *
     * @param encodedOrderId The Base64-encoded order ID string.
     * @return The decoded order ID string in its original format.
     */
    public static String decodeOrderReference(String encodedOrderId) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedOrderId);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}