package greencity.mapping;

import greencity.dto.CoordinatesDto;
import greencity.entity.coords.Coordinates;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link Coordinates} into
 * {@link CoordinatesDto}.
 */
@Component
public class CoordinateDtoMapper extends AbstractConverter<Coordinates, CoordinatesDto> {
    /**
     * Method convert {@link Coordinates} to {@link CoordinatesDto}.
     *
     * @return {@link CoordinatesDto}
     */
    @Override
    protected CoordinatesDto convert(Coordinates coordinates) {
        return CoordinatesDto.builder()
            .latitude(coordinates.getLatitude())
            .longitude(coordinates.getLongitude())
            .build();
    }
}
