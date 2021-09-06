package greencity.service.ubs;

import greencity.dto.GetAllFieldsMainDto;

import java.util.List;

public interface AllValuesFromTableService {
    /**
     * {@inheritDoc}
     */
    List<GetAllFieldsMainDto> findAllValues();
}
