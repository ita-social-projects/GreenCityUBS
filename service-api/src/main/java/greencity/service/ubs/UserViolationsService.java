package greencity.service.ubs;

import greencity.dto.UserViolationsWithUserName;
import greencity.entity.enums.SortingOrder;
import org.springframework.data.domain.Pageable;

public interface UserViolationsService {
    /**
     * Method returns all violations by userId.
     *
     * @param userId of {@link Long} administrator's user id;
     * @author Roman Sulymka
     */
    UserViolationsWithUserName getAllViolations(Pageable page, Long userId, String columnName,
        SortingOrder sortingOrder);
}
