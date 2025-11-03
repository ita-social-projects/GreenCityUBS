package greencity.service.ubs.calculator;

import greencity.dto.bag.BagInfoDto;
import greencity.dto.order.OrderInfoDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.payment.PaymentWithStatusDto;
import greencity.dto.user.UserPointDto;
import greencity.enums.PaymentStatus;
import java.util.List;

/**
 * Service for calculating payment-related information for orders. Handles sums
 * with/without discounts, and the amount already paid.
 */
public interface PaymentCalculatorService {
    /**
     * Calculates the final amount to pay for an order in coins, considering bags,
     * certificates, points, and previous payments.
     *
     * @param dto        request DTO with payment/order details
     * @param orderInfo  the order to calculate
     * @param userPoints the user's points who placed the order
     * @return final amount to pay in coins
     */
    long calculateSumToPay(OrderWayForPayClientDto dto, OrderInfoDto orderInfo, UserPointDto userPoints);

    /**
     * Counts the total amount that has already been paid for an order. Only
     * payments with status {@link PaymentStatus#PAID} are included.
     *
     * @param payments list of payments for the order
     * @return total paid amount, or 0 if none
     */
    long countPaidAmount(List<PaymentWithStatusDto> payments);

    /**
     * Calculates the full sum of an order without applying any discounts, bonuses,
     * or certificates. Based only on bag prices and quantities.
     *
     * @param getOrderBagsAndQuantity list of order bags with their quantities
     * @return order sum in coins without discounts
     */
    long calculateOrderSumWithoutDiscounts(List<BagInfoDto> getOrderBagsAndQuantity);
}
