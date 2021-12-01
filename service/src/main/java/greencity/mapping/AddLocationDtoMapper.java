package greencity.mapping;

import greencity.dto.AddLocationDto;
import greencity.dto.AddLocationTranslationDto;
import greencity.entity.user.Location;
import greencity.entity.user.LocationTranslation;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AddLocationDtoMapper extends AbstractConverter<Location, AddLocationDto> {
    @Override
    protected AddLocationDto convert(Location source) {
        List<LocationTranslation> locationTranslations = new ArrayList<>(source.getLocationTranslations());
        List<AddLocationTranslationDto> dtos = locationTranslations.stream().map(
            i -> new AddLocationTranslationDto(i.getLocationName(), i.getLocationName(), i.getLanguage().getId()))
            .collect(Collectors.toList());
        return AddLocationDto.builder()
            .addLocationDtoList(dtos).build();
    }
}
