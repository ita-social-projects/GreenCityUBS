package greencity.service.ubs;

import greencity.dto.UserWithViolationsDto;

public interface UserViolationsService {
    /**
     * Method returns all violations by userId.
     *
     * @param userId of {@link Long} administrator's user id;
     * @author Roman Sulymka
     */
    UserWithViolationsDto getAllViolations(Long userId);
}
