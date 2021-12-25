package greencity.mapping;

import greencity.dto.LocationInfoDto;
import greencity.dto.LocationTranslationDto;
import greencity.dto.LocationsDto;
import greencity.dto.RegionTranslationDto;
import greencity.entity.user.Region;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FindAllInfoAboutLocationDtoMapper extends AbstractConverter<Region, LocationInfoDto> {
    @Override
    protected LocationInfoDto convert(Region source) {
        List<LocationsDto> locationsDtoList = source.getLocations().stream()
            .map(i -> new LocationsDto(i.getId(), i.getLocationStatus().toString(), i.getCoordinates().getLatitude(),
                i.getCoordinates().getLongitude(),
                i.getLocationTranslations().stream()
                    .map(j -> new LocationTranslationDto(j.getLocationName(), j.getLanguage().getCode()))
                    .collect(Collectors.toList())))
            .collect(Collectors.toList());
        List<RegionTranslationDto> regionTranslationDtoList = source.getRegionTranslations().stream()
            .map(i -> new RegionTranslationDto(i.getName(), i.getLanguage().getCode())).collect(Collectors.toList());
        return LocationInfoDto.builder()
            .regionId(source.getId())
            .locationsDto(locationsDtoList)
            .regionTranslationDtos(regionTranslationDtoList)
            .build();
    }
}
