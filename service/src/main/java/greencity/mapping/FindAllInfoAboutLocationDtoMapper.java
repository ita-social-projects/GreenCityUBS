package greencity.mapping;

import greencity.dto.location.LocationInfoDto;
import greencity.dto.location.LocationTranslationDto;
import greencity.dto.location.LocationsDto;
import greencity.dto.location.RegionTranslationDto;
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
                .locationTranslationDtoList(List.of(
                        LocationTranslationDto.builder().locationName(location.getNameUk()).languageCode("ua").build(),
                        LocationTranslationDto.builder().locationName(location.getNameEn()).languageCode("en").build()))
                .build())
            .collect(Collectors.toList());

        List<RegionTranslationDto> regionTranslationDtoList = List.of(
                RegionTranslationDto.builder().regionName(source.getUkrName()).languageCode("ua").build(),
                RegionTranslationDto.builder().regionName(source.getEnName()).languageCode("en").build());


        return LocationInfoDto.builder()
            .regionId(source.getId())
            .locationsDto(locationsDtoList)
            .regionTranslationDtos(regionTranslationDtoList)
            .build();
    }
}
