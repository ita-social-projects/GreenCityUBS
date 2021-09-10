package greencity.mapping;

import greencity.dto.ColumnStateDTO;
import greencity.entity.parameters.ColumnState;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link ColumnStateDTO} into
 * {@link ColumnState}.
 */
@Component
public class ColumnStateDtoMapper extends AbstractConverter<ColumnStateDTO, ColumnState> {
    /**
     * Method convert {@link ColumnStateDTO} to {@link ColumnState}.
     *
     * @return {@link ColumnState}
     */
    @Override
    protected ColumnState convert(ColumnStateDTO source) {
        return new ColumnState();
    }
}
