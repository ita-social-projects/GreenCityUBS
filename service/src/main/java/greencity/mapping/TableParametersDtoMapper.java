package greencity.mapping;

import greencity.dto.TableParamsDTO;
import greencity.entity.parameters.TableParameters;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link TableParamsDTO} into
 * {@link TableParameters}.
 */
@Component
public class TableParametersDtoMapper extends AbstractConverter<TableParamsDTO, TableParameters> {
    /**
     * Method convert {@link TableParamsDTO} to {@link TableParameters}.
     *
     * @return {@link TableParameters}
     */
    @Override
    protected TableParameters convert(TableParamsDTO source) {
        return new TableParameters();
    }
}
