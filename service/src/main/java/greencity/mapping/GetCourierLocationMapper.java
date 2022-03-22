package greencity.mapping;

import greencity.dto.*;
import greencity.entity.order.CourierLocation;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetCourierLocationMapper extends AbstractConverter<CourierLocation, GetCourierLocationDto> {
    @Override
    protected GetCourierLocationDto convert(CourierLocation source) {
        List<CourierDto> courierDtoList = List.of(CourierDto.builder()
            .courierId(source.getCourier().getId())
            .courierStatus(source.getCourier().getCourierStatus().toString())
            .courierTranslationDtos(source.getCourier().getCourierTranslationList().stream()
                .map(courierTranslation -> CourierTranslationDto.builder()
                    .languageCode(courierTranslation.getLanguage().getCode())
                    .name(courierTranslation.getName())
                    .build())
                .collect(Collectors.toList()))
            .build());

        List<LocationTranslationDto> locationTranslationDtos = source.getLocation().getLocationTranslations().stream()
            .map(locationTranslation -> LocationTranslationDto.builder()
                .locationName(locationTranslation.getLocationName())
                .languageCode(locationTranslation.getLanguage().getCode())
                .build())
            .collect(Collectors.toList());

        List<LocationsDto> locationsDtos = List.of(LocationsDto.builder()
            .locationId(source.getLocation().getId())
            .latitude(source.getLocation().getCoordinates().getLatitude())
            .longitude(source.getLocation().getCoordinates().getLongitude())
            .locationStatus(source.getLocation().getLocationStatus().toString())
            .locationTranslationDtoList(locationTranslationDtos)
            .build());

        List<RegionTranslationDto> regionTranslationDtos =
            source.getLocation().getRegion().getRegionTranslations().stream()
                .map(regionTranslation -> RegionTranslationDto.builder()
                    .regionName(regionTranslation.getName())
                    .languageCode(regionTranslation.getLanguage().getCode())
                    .build())
                .collect(Collectors.toList());

        List<LocationInfoDto> locationInfoDtoList = List.of(LocationInfoDto.builder()
            .locationsDto(locationsDtos)
            .regionId(source.getLocation().getRegion().getId())
            .regionTranslationDtos(regionTranslationDtos)
            .build());

        return GetCourierLocationDto.builder()
            .courierLocationId(source.getId())
            .courierLimit(source.getCourierLimit().toString())
            .maxAmountOfBigBags(source.getMaxAmountOfBigBags())
            .minAmountOfBigBags(source.getMinAmountOfBigBags())
            .maxPriceOfOrder(source.getMaxPriceOfOrder())
            .minPriceOfOrder(source.getMinPriceOfOrder())
            .courierDtos(courierDtoList)
            .locationInfoDtos(locationInfoDtoList)
            .build();
    }
}
