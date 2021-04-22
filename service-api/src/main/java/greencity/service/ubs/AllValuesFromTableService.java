package greencity.service.ubs;

import greencity.dto.GetAllFieldsMainDto;
import greencity.entity.allfieldsordertable.GetAllValuesFromTable;
import java.util.List;

public interface AllValuesFromTableService {
    /**
     * {@inheritDoc}
     */
    List<GetAllFieldsMainDto> findAllValues();
}
