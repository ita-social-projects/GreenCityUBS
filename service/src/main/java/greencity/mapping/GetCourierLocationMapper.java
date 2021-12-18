package greencity.mapping;

import greencity.dto.*;
import greencity.entity.order.Courier;
import greencity.entity.order.CourierLocation;
import greencity.entity.user.Location;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetCourierLocationMapper extends AbstractConverter<CourierLocation, GetCourierLocationDto> {
    @Override
    protected GetCourierLocationDto convert(CourierLocation source) {
        List<Courier> couriers = List.of(source.getCourier());
        List<CourierDto> courierDtos = couriers.stream()
            .map(i -> new CourierDto(i.getId(), i.getCourierStatus().toString(),
                i.getCourierTranslationList().stream().map(j -> new CourierTranslationDto(
                    j.getName(), j.getLimitDescription(), j.getLanguage().getCode())).collect(Collectors.toList())))
            .collect(Collectors.toList());
        List<Location> locations = List.of(source.getLocation());
        List<LocationsDto> locationsDtos = locations.stream().map(
            i -> new LocationsDto(i.getId(), i.getLocationStatus().toString(), i.getLocationTranslations().stream().map(
                j -> new LocationTranslationDto(j.getLocationName(), j.getLanguage().getCode(), j.getRegion()))
                .collect(Collectors.toList())))
            .collect(Collectors.toList());
        return GetCourierLocationDto.builder()
            .courierLocationId(source.getId())
            .courierLimit(source.getCourierLimit().toString())
            .maxAmountOfBigBags(source.getMaxAmountOfBigBags())
            .minAmountOfBigBags(source.getMinAmountOfBigBags())
            .maxPriceOfOrder(source.getMaxPriceOfOrder())
            .minPriceOfOrder(source.getMinPriceOfOrder())
            .courierDtos(courierDtos)
            .locationsDtos(locationsDtos)
            .build();
    }
}
