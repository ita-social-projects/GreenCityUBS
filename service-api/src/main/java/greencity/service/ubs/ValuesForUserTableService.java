package greencity.service.ubs;

import greencity.dto.order.UserWithSomeOrderDetailAndChatIdDto;
import greencity.dto.pageble.PageableDto;
import greencity.enums.SortingOrder;
import greencity.dto.filters.CustomerPage;
import greencity.dto.filters.UserFilterCriteria;

public interface ValuesForUserTableService {
    /**
     * Method that returns users that have made at least one order.
     *
     * @return {@link UserWithSomeOrderDetailAndChatIdDto}.
     * @author Stepan Tehlivets.
     */
    PageableDto<UserWithSomeOrderDetailAndChatIdDto> getAllFields(CustomerPage page, String columnName,
        SortingOrder sortingOrder, UserFilterCriteria userFilterCriteria, String email);
}
