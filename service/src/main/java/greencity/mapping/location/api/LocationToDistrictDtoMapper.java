package greencity.mapping.location.api;

import greencity.dto.location.api.DistrictDto;
import greencity.dto.location.api.LocationDto;
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
public class LocationToDistrictDtoMapper extends AbstractConverter<LocationDto, DistrictDto> {
    @Override
    protected DistrictDto convert(LocationDto locationDto) {
        return DistrictDto.builder()
            .nameUa(locationDto.getLocationNameMap().get("name"))
            .nameEn(locationDto.getLocationNameMap().get("name_en"))
            .build();
    }
}
