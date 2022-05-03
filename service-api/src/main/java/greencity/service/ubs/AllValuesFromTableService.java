package greencity.service.ubs;

import greencity.dto.table.GetAllFieldsMainDto;

import java.util.List;

public interface AllValuesFromTableService {
    /**
     * {@inheritDoc}
     */
    List<GetAllFieldsMainDto> findAllValues();
}
