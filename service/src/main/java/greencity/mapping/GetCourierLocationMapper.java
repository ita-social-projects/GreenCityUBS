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
                    .limitDescription(courierTranslation.getLimitDescription())
                    .build())
                .collect(Collectors.toList()))
            .build());

        List<LocationsDto> locationsDtoList = List.of(LocationsDto.builder()
            .locationStatus(source.getLocation().getLocationStatus().toString())
            .locationId(source.getLocation().getId())
            .longitude(source.getLocation().getCoordinates().getLongitude())
            .latitude(source.getLocation().getCoordinates().getLatitude())
            .locationTranslationDtoList(source.getLocation().getLocationTranslations().stream()
                .map(locationTranslation -> LocationTranslationDto.builder()
                    .locationName(locationTranslation.getLocationName())
                    .languageCode(locationTranslation.getLanguage().getCode())
                    .build())
                .collect(Collectors.toList()))
            .build());

        return GetCourierLocationDto.builder()
            .courierLocationId(source.getId())
            .courierLimit(source.getCourierLimit().toString())
            .maxAmountOfBigBags(source.getMaxAmountOfBigBags())
            .minAmountOfBigBags(source.getMinAmountOfBigBags())
            .maxPriceOfOrder(source.getMaxPriceOfOrder())
            .minPriceOfOrder(source.getMinPriceOfOrder())
            .courierDtos(courierDtoList)
            .locationsDtos(locationsDtoList)
            .build();
    }
}
