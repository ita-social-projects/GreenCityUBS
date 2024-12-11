package greencity.util;

import greencity.entity.order.Order;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Component
@ToString
public class OrderUtils {
    private OrderUtils() {
    }

    /**
     * The method that generates the order id to be paid.
     *
     * @param orderId - order id user
     * @param order   {@link Order} - get order user
     * @return {@String} - orderId.
     */
    public static String generateOrderIdForPayment(Long orderId, Order order) {
        int lastNumber = order.getPayment().size() - 1;
        return String.format("%s_%s_%s", orderId, order.getCounterOrderPaymentId(),
            order.getPayment().get(lastNumber).getId());
    }
}