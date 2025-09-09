package greencity.util;

import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.exceptions.DecodeOrderReferenceException;
import greencity.exceptions.payment.InvalidPaymentResponseException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@ToString
@Slf4j
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
        String rawOrderId = String.format("%s_%s_%s", orderId,
            (order.getCounterOrderPaymentId() == null) ? 1 : order.getCounterOrderPaymentId(),
            getLastPayment(order).getId());

        return Base64.getEncoder()
            .withoutPadding()
            .encodeToString(rawOrderId.getBytes(StandardCharsets.UTF_8));
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

    /**
     * Iterates through order payments and gets one with maximum id, which is
     * corresponding the last one.
     *
     * @param order The {@link Order} object containing details such as payment and
     *              counterOrderPaymentId.
     * @return An {@link Payment} object containing corresponding data.
     */
    public static Payment getLastPayment(Order order) {
        return order.getPayment().stream()
            .filter(payment -> payment.getId() != null)
            .max(Comparator.comparing(Payment::getId))
            .orElseThrow(() -> new IllegalStateException("No payment found"));
    }

    //TODO add tests
    /**
     * Extracts a numeric ID from a Base64-encoded order reference string.
     * The reference is expected in format: orderId_counterOrderPaymentId_paymentId.
     * After decoding and splitting by "_", this method returns the part at the given index.
     * Index mapping:
     * 0 → orderId
     * 1 → counterOrderPaymentId
     * 2 → paymentId
     *
     * @param orderReference the Base64-encoded order reference
     * @param index          index of the part to extract
     * @return the extracted ID as Long
     * @throws InvalidPaymentResponseException if the format is invalid, index is out of bounds,
     *                                         or the extracted part is not a number
     */
    public static Long getIdByOrderReference(String orderReference, int index) {
        try {
            String decoded = decodeOrderReference(orderReference);
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