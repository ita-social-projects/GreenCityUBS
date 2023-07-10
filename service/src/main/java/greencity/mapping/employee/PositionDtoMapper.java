package greencity.mapping.employee;

import greencity.dto.position.PositionDto;
import greencity.entity.user.employee.Position;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link Position} into
 * {@link PositionDto}.
 */
@Component
public class PositionDtoMapper extends AbstractConverter<Position, PositionDto> {
    /**
     * Method convert {@link Position} to {@link PositionDto}.
     *
     * @return {@link PositionDto}
     */
    @Override
    protected PositionDto convert(Position position) {
        return PositionDto.builder()
            .id(position.getId())
            .name(position.getName())
            .nameEn(position.getNameEn())
            .build();
    }
}
