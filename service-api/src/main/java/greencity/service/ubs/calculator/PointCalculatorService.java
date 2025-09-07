package greencity.service.ubs.calculator;

import greencity.dto.order.OrderWayForPayClientDto;
import greencity.entity.user.User;

//TODO add docs
//TODO add tests
public interface PointCalculatorService {
    long reduceOrderSumDueToUsedPoints(long sumToPayInCoins, int pointsToUse);

    long getPointSumToPayInCoins(OrderWayForPayClientDto dto, User currentUser, long sumToPayInCoins);
}
