package greencity.mapping;

import greencity.dto.AddLocationTranslationDto;
import greencity.dto.LocationCreateDto;
import greencity.dto.RegionTranslationDto;
import greencity.entity.user.Location;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocationCreateDtoMapper extends AbstractConverter<Location, LocationCreateDto> {
    @Override
    protected LocationCreateDto convert(Location source) {
        List<AddLocationTranslationDto> dtos = source.getLocationTranslations().stream().map(
            i -> new AddLocationTranslationDto(i.getLocationName(), i.getLanguage().getCode()))
            .collect(Collectors.toList());
        List<RegionTranslationDto> regionTranslationDtoList = source.getRegion().getRegionTranslation().stream().map(
            i -> new RegionTranslationDto(i.getName(), i.getLanguage().getCode())).collect(Collectors.toList());
        return LocationCreateDto.builder()
            .addLocationDtoList(dtos)
            .longitude(source.getCoordinates().getLongitude())
            .latitude(source.getCoordinates().getLatitude())
            .regionTranslationDtos(regionTranslationDtoList)
            .build();
    }
}
