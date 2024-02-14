package greencity.mapping.location;

import greencity.dto.location.AddLocationTranslationDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.RegionTranslationDto;
import greencity.entity.user.Location;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class LocationCreateDtoMapper extends AbstractConverter<Location, LocationCreateDto> {
    private static final String UALangCode = "ua";
    private static final String ENLangCode = "en";

    @Override
    protected LocationCreateDto convert(Location source) {
        List<AddLocationTranslationDto> locationTranslationDtoList = List.of(
            AddLocationTranslationDto.builder().languageCode(UALangCode).locationName(source.getNameUk()).build(),
            AddLocationTranslationDto.builder().languageCode(ENLangCode).locationName(source.getNameEn()).build());

        List<RegionTranslationDto> regionTranslationDtoList = List.of(
            RegionTranslationDto.builder().languageCode(UALangCode).regionName(source.getRegion().getUkrName()).build(),
            RegionTranslationDto.builder().languageCode(ENLangCode).regionName(source.getRegion().getEnName()).build());

        return LocationCreateDto.builder()
            .addLocationDtoList(locationTranslationDtoList)
            .longitude(source.getCoordinates().getLongitude())
            .latitude(source.getCoordinates().getLatitude())
            .regionTranslationDtos(regionTranslationDtoList)
            .build();
    }
}
