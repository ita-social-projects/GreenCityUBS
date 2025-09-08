package greencity.service.ubs.calculator;

import greencity.dto.order.OrderWayForPayClientDto;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.entity.order.Payment;
import greencity.entity.user.User;
import greencity.enums.PaymentStatus;
import java.util.List;

//TODO add tests
/**
 * Service for calculating payment-related information for orders.
 * Handles sums with/without discounts, and the amount already paid.
 */
public interface PaymentCalculatorService {
    /**
     * Calculates the final amount to pay for an order in coins,
     * considering bags, certificates, points, and previous payments.
     *
     * @param dto         request DTO with payment/order details
     * @param order       the order to calculate
     * @param currentUser the user who placed the order
     * @return final amount to pay in coins
     */
    long calculateSumToPay(OrderWayForPayClientDto dto, Order order, User currentUser);

    /**
     * Counts the total amount that has already been paid for an order.
     * Only payments with status {@link PaymentStatus#PAID} are included.
     *
     * @param payments list of payments for the order
     * @return total paid amount, or 0 if none
     */
    Long countPaidAmount(List<Payment> payments);

    /**
     * Calculates the full sum of an order without applying any discounts,
     * bonuses, or certificates. Based only on bag prices and quantities.
     *
     * @param getOrderBagsAndQuantity list of order bags with their quantities
     * @return order sum in coins without discounts
     */
    long calculateOrderSumWithoutDiscounts(List<OrderBag> getOrderBagsAndQuantity);
}
