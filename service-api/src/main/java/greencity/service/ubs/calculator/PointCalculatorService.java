package greencity.service.ubs.calculator;

import greencity.dto.order.OrderWayForPayClientDto;
import greencity.entity.user.User;

/**
 * Service for calculating order payment amount with respect to user bonus
 * points.
 */
public interface PointCalculatorService {
    /**
     * Reduces the order amount (in coins) by subtracting the value of used points.
     *
     * @param sumToPayInCoins total amount to pay in coins before applying points
     * @param pointsToUse     number of bonus points the user wants to apply
     * @return updated amount to pay in coins after applying points
     */
    long reduceOrderSumDueToUsedPoints(long sumToPayInCoins, int pointsToUse);

    /**
     * Calculates the final order amount (in coins) after validating and applying
     * user's bonus points.
     *
     * @param dto             order payment data containing requested points to use
     * @param currentUser     user who makes the order
     * @param sumToPayInCoins initial amount to pay in coins
     * @return final amount to pay in coins after applying points
     */
    long getPointSumToPayInCoins(OrderWayForPayClientDto dto, User currentUser, long sumToPayInCoins);
}
