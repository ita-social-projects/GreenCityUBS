package greencity.service.ubs;

import greencity.dto.FieldsForUsersTableDto;
import greencity.dto.PageableDto;
import greencity.dto.UserWithSomeOrderDetailDto;
import greencity.entity.enums.SortingOrder;
import greencity.filters.UserFilterCriteria;
import org.springframework.data.domain.Pageable;

public interface ValuesForUserTableService {
    /**
     * Method that returns users that have made at least one order.
     *
     * @return {@link FieldsForUsersTableDto}.
     * @author Stepan Tehlivets.
     */
    PageableDto<UserWithSomeOrderDetailDto> getAllFields(Pageable page, String columnName,
        SortingOrder sortingOrder, UserFilterCriteria userFilterCriteria);
}
