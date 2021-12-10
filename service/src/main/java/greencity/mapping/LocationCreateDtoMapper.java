package greencity.mapping;

import greencity.dto.AddLocationTranslationDto;
import greencity.dto.LocationCreateDto;
import greencity.entity.user.Location;
import greencity.entity.user.LocationTranslation;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocationCreateDtoMapper extends AbstractConverter<Location, LocationCreateDto> {
    @Override
    protected LocationCreateDto convert(Location source) {
        List<LocationTranslation> locationTranslations = source.getLocationTranslations();
        List<AddLocationTranslationDto> dtos = locationTranslations.stream().map(
            i -> new AddLocationTranslationDto(i.getLocationName(), i.getLocationName(), i.getLanguage().getId()))
            .collect(Collectors.toList());
        return LocationCreateDto.builder()
            .addLocationDtoList(dtos).build();
    }
}
