package greencity.mapping.location;

import greencity.dto.LocationsDto;
import greencity.entity.user.Location;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class LocationToLocationsDtoMapper extends AbstractConverter<Location, LocationsDto> {
    @Override
    public LocationsDto convert(Location location) {
        return LocationsDto.builder()
            .id(location.getId())
            .locationStatus(location.getLocationStatus().name())
            .regionNameUk(String.valueOf(location.getRegion().getNameUk()))
            .regionNameEn(String.valueOf(location.getRegion().getNameEn()))
            .latitude(location.getCoordinates().getLatitude())
            .longitude(location.getCoordinates().getLongitude())
            .nameUk(location.getNameUk())
            .nameEn(location.getNameEn())
            .build();
    }
}
