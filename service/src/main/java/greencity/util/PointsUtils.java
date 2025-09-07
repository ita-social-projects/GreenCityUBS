package greencity.util;

import greencity.exceptions.BadRequestException;
import org.springframework.stereotype.Service;

@Service
public class PointsUtils {
    //TODO add docs
    //TODO add test
    public void checkIfUserHaveEnoughPoints(Integer userPoints, Integer requiredPoints) {
        if (userPoints < requiredPoints) {
            throw new BadRequestException("User doesn't have enough points");
        }
    }
}
