package greencity.mapping.employee;

import greencity.dto.position.PositionDto;
import greencity.entity.user.employee.Position;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link PositionDto} into
 * {@link Position}.
 */
@Component
public class PositionMapper extends AbstractConverter<PositionDto, Position> {
    /**
     * Method convert {@link PositionDto} to {@link Position}.
     *
     * @return {@link Position}
     */
    @Override
    protected Position convert(PositionDto positionDto) {
        return Position.builder()
                .id(positionDto.getId())
                .name(positionDto.getName())
                .name_eng(positionDto.getName_eng())
                .build();
    }
}
