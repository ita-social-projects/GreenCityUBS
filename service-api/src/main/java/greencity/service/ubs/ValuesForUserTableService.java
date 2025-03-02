package greencity.service.ubs;

import greencity.dto.order.UserWithSomeOrderDetailDto;
import greencity.dto.pageble.PageableDto;
import greencity.enums.SortingOrder;
import greencity.filters.CustomerPage;
import greencity.filters.UserFilterCriteria;

public interface ValuesForUserTableService {
    /**
     * Method that returns users that have made at least one order.
     *
     * @return {@link UserWithSomeOrderDetailDto}.
     * @author Stepan Tehlivets.
     */
    PageableDto<UserWithSomeOrderDetailDto> getAllFields(CustomerPage page, String columnName,
        SortingOrder sortingOrder, UserFilterCriteria userFilterCriteria, String email);
}
