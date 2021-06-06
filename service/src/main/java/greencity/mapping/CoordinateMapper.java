package greencity.mapping;

import greencity.dto.CoordinatesDto;
import greencity.entity.coords.Coordinates;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link CoordinatesDto} into
 * {@link Coordinates}.
 */
@Component
public class CoordinateMapper extends AbstractConverter<CoordinatesDto, Coordinates> {
    /**
     * Method convert {@link CoordinatesDto} to {@link Coordinates}.
     *
     * @return {@link Coordinates}
     */
    @Override
    protected Coordinates convert(CoordinatesDto coordinatesDto) {
        return Coordinates.builder()
            .latitude(coordinatesDto.getLatitude())
            .longitude(coordinatesDto.getLongitude())
            .build();
    }
}
