package greencity.service.ubs.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.user.UserPointDto;
import greencity.entity.user.User;
import greencity.exceptions.BadRequestException;
import greencity.util.PointsUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointCalculatorServiceImplTest {

    @Mock
    private PointsUtils pointsUtils;

    @InjectMocks
    private PointCalculatorServiceImpl pointCalculatorService;

    @Test
    void reduceOrderSumDueToUsedPoints_ShouldReduceCorrectly_WhenEnoughSum() {
        long sumToPay = 300L;
        int pointsToUse = 2;

        long result = pointCalculatorService.reduceOrderSumDueToUsedPoints(sumToPay, pointsToUse);

        assertEquals(100L, result);
    }

    @Test
    void reduceOrderSumDueToUsedPoints_ShouldNotReduce_WhenNotEnoughSum() {
        long sumToPay = 5L;
        int pointsToUse = 10;

        long result = pointCalculatorService.reduceOrderSumDueToUsedPoints(sumToPay, pointsToUse);

        assertEquals(sumToPay, result);
    }

    @Test
    void getPointSumToPayInCoins_ShouldCallUtilsAndReduceSum() {
        OrderWayForPayClientDto dto = new OrderWayForPayClientDto();
        dto.setPointsToUse(3);

        UserPointDto user = new UserPointDto();
        user.setPoints(10);

        long sumToPay = 200L;

        doNothing().when(pointsUtils)
            .checkIfUserHasEnoughPoints(user.getPoints(), dto.getPointsToUse());

        long result = pointCalculatorService.getPointSumToPayInCoins(dto, user, sumToPay);

        assertEquals(200L, result);
        verify(pointsUtils).checkIfUserHasEnoughPoints(user.getPoints(), dto.getPointsToUse());
    }

    @Test
    void getPointSumToPayInCoins_ShouldThrowException_WhenNotEnoughPoints() {
        OrderWayForPayClientDto dto = new OrderWayForPayClientDto();
        dto.setPointsToUse(20);

        UserPointDto user = new UserPointDto();
        user.setPoints(5);

        doThrow(new BadRequestException("User doesn't have enough points"))
            .when(pointsUtils).checkIfUserHasEnoughPoints(user.getPoints(), dto.getPointsToUse());

        assertThrows(BadRequestException.class, () -> pointCalculatorService.getPointSumToPayInCoins(dto, user, 100L));
    }
}