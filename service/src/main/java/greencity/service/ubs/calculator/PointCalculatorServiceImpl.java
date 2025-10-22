package greencity.service.ubs.calculator;

import greencity.constant.AppConstant;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.user.UserPointDto;
import greencity.util.PointsUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointCalculatorServiceImpl implements PointCalculatorService {
    private final PointsUtils pointsUtils;

    @Override
    public long reduceOrderSumDueToUsedPoints(long sumToPayInCoins, int pointsToUse) {
        if (sumToPayInCoins >= pointsToUse * (long) AppConstant.CURRENCY_CONVERSION_RATE) {
            sumToPayInCoins -= pointsToUse * (long) AppConstant.CURRENCY_CONVERSION_RATE;
        }
        return sumToPayInCoins;
    }

    @Override
    public long getPointSumToPayInCoins(OrderWayForPayClientDto dto, UserPointDto userPoints, long sumToPayInCoins) {
        pointsUtils.checkIfUserHasEnoughPoints(userPoints.getPoints(), dto.getPointsToUse());
        sumToPayInCoins = reduceOrderSumDueToUsedPoints(sumToPayInCoins, dto.getPointsToUse());
        return sumToPayInCoins;
    }
}
