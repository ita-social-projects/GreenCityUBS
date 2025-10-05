package greencity.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import greencity.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointsUtilsTest {
    @InjectMocks
    private PointsUtils pointsUtils;

    @Test
    void userHasEnoughPoints_noExceptionThrown() {
        Integer userPoints = 10;
        Integer requiredPoints = 5;

        assertDoesNotThrow(() -> pointsUtils.checkIfUserHaveEnoughPoints(userPoints, requiredPoints));
    }

    @Test
    void userHasNotEnoughPoints_throwsException() {
        Integer userPoints = 3;
        Integer requiredPoints = 5;

        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> pointsUtils.checkIfUserHaveEnoughPoints(userPoints, requiredPoints));

        assertEquals("User doesn't have enough points", exception.getMessage());
    }
}