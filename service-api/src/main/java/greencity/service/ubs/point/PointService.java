package greencity.service.ubs.point;

import greencity.dto.user.AllPointsUserDto;
import greencity.entity.user.User;

//TODO add test
public interface PointService {
    /**
     * Method returns list all bonuses of user.
     *
     * @param uuid of {@link User}'s uuid;
     * @return {@link AllPointsUserDto} that contains all client's bonuses;
     * @author Liubomyr Bratakh
     */
    AllPointsUserDto findAllCurrentPointsForUser(String uuid);
}
