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

    private static final String UALangCode = "ua";
    private static final String ENLangCode = "en";

    @Override
    protected LocationCreateDto convert(Location source) {
        List<AddLocationTranslationDto> locationTranslationDtoList = List.of(
                AddLocationTranslationDto.builder().languageCode(UALangCode).locationName(source.getNameUk()).build(),
                AddLocationTranslationDto.builder().languageCode(ENLangCode).locationName(source.getNameEn()).build());
                /*-
                source.getLocationTranslations().stream()
            .map(locationTranslation -> AddLocationTranslationDto.builder()
                .languageCode(locationTranslation.getLanguage().getCode())
                .locationName(locationTranslation.getLocationName())
                .build())
            .collect(Collectors.toList());
*/
        List<RegionTranslationDto> regionTranslationDtoList = List.of(
                RegionTranslationDto.builder().languageCode(UALangCode).regionName(source.getRegion().getUkrName()).build(),
                RegionTranslationDto.builder().languageCode(ENLangCode).regionName(source.getRegion().getEnName()).build());
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
