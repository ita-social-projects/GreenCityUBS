package greencity.mapping.location;

import greencity.dto.location.LocationTranslationDto;
import greencity.dto.location.LocationsDto;
import greencity.entity.user.Location;
import java.util.ArrayList;
import java.util.List;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class LocationToLocationsDtoMapper extends AbstractConverter<Location, LocationsDto> {
    @Override
    public LocationsDto convert(Location location) {
        List<LocationTranslationDto> locationTranslations = new ArrayList<>();
        locationTranslations.add(LocationTranslationDto.builder()
            .locationName(location.getNameUk())
            .languageCode("uk")
            .build());
        locationTranslations.add(LocationTranslationDto.builder()
            .locationName(location.getNameEn())
            .languageCode("en")
            .build());

        return LocationsDto.builder()
            .locationId(location.getId())
            .locationStatus(location.getLocationStatus().name())
            .latitude(location.getCoordinates().getLatitude())
            .longitude(location.getCoordinates().getLongitude())
            .locationTranslationDtoList(locationTranslations)
            .build();
    }
}
