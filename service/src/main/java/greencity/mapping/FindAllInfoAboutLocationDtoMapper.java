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
            .map(location -> LocationsDto.builder()
                .locationId(location.getId())
                .longitude(location.getCoordinates().getLongitude())
                .latitude(location.getCoordinates().getLatitude())
                .locationStatus(location.getLocationStatus().toString())
                .locationTranslationDtoList(location.getLocationTranslations().stream()
                    .map(locationTranslation -> LocationTranslationDto.builder()
                        .languageCode(locationTranslation.getLanguage().getCode())
                        .locationName(locationTranslation.getLocationName())
                        .build())
                    .collect(Collectors.toList()))
                .build())
            .collect(Collectors.toList());

        List<RegionTranslationDto> regionTranslationDtoList = source.getRegionTranslations().stream()
            .map(regionTranslation -> RegionTranslationDto.builder()
                .regionName(regionTranslation.getName())
                .languageCode(regionTranslation.getLanguage().getCode())
                .build())
            .collect(Collectors.toList());

        return LocationInfoDto.builder()
            .regionId(source.getId())
            .locationsDto(locationsDtoList)
            .regionTranslationDtos(regionTranslationDtoList)
            .build();
    }
}
