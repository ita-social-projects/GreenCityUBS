package greencity.service.ubs.point;

import greencity.dto.user.AllPointsUserDto;

public interface PointService {
    /**
     * Method returns list all bonuses of user.
     *
     * @param uuid of user's uuid;
     * @return {@link AllPointsUserDto} that contains all client's bonuses;
     * @author Liubomyr Bratakh
     */
    AllPointsUserDto findAllCurrentPointsForUser(String uuid);
}
