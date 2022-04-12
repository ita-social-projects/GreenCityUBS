package greencity.mapping;

import greencity.dto.location.AddLocationTranslationDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.RegionTranslationDto;
import greencity.entity.user.Location;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocationCreateDtoMapper extends AbstractConverter<Location, LocationCreateDto> {
    @Override
    protected LocationCreateDto convert(Location source) {
        List<AddLocationTranslationDto> locationTranslationDtoList = List.of(
                AddLocationTranslationDto.builder().languageCode("ua").locationName(source.getNameUk()).build(),
                AddLocationTranslationDto.builder().languageCode("en").locationName(source.getNameEn()).build());
                /*-
                source.getLocationTranslations().stream()
            .map(locationTranslation -> AddLocationTranslationDto.builder()
                .languageCode(locationTranslation.getLanguage().getCode())
                .locationName(locationTranslation.getLocationName())
                .build())
            .collect(Collectors.toList());
*/
        List<RegionTranslationDto> regionTranslationDtoList = List.of(
                RegionTranslationDto.builder().languageCode("ua").regionName(source.getRegion().getUkrName()).build(),
                RegionTranslationDto.builder().languageCode("en").regionName(source.getRegion().getEnName()).build());
                /*-
                source.getRegion().getRegionTranslations().stream()
            .map(regionTranslation -> RegionTranslationDto.builder()
                .regionName(regionTranslation.getName())
                .languageCode(regionTranslation.getLanguage().getCode())
                .build())
            .collect(Collectors.toList());
*/
        return LocationCreateDto.builder()
            .addLocationDtoList(locationTranslationDtoList)
            .longitude(source.getCoordinates().getLongitude())
            .latitude(source.getCoordinates().getLatitude())
            .regionTranslationDtos(regionTranslationDtoList)
            .build();
    }
}
