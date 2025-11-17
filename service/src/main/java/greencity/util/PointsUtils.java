package greencity.util;

import greencity.exceptions.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PointsUtils {
    /**
     * Checks if a user has enough points to perform an action.
     *
     * @param userPoints     the current number of points the user has
     * @param requiredPoints the number of points required to perform the action
     * @throws BadRequestException if the user does not have enough points
     */
    public void checkIfUserHasEnoughPoints(Integer userPoints, Integer requiredPoints) {
        if (userPoints < requiredPoints) {
            throw new BadRequestException("User doesn't have enough points");
        }
    }
}
