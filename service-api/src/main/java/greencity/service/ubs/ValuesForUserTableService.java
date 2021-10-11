package greencity.service.ubs;

import greencity.dto.FieldsForUsersTableDto;

public interface ValuesForUserTableService {
    /**
     * Method that returns users that have made at least one order.
     *
     * @return {@link FieldsForUsersTableDto}.
     * @author Stepan Tehlivets.
     */
    FieldsForUsersTableDto getAllFields();
}
