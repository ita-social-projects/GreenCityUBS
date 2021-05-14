package greencity.service.ubs;

import greencity.dto.GetAllFieldsMainDto;
import java.util.List;

public interface AllValuesFromTableSortingService {
    /**
     * {@inheritDoc}
     */
    List<GetAllFieldsMainDto> getAllSortingValues(String columnName, String sortingType);
}
