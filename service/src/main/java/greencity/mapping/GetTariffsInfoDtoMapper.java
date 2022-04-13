package greencity.mapping;

import greencity.dto.courier.CourierTranslationDto;
import greencity.dto.tariff.GetTariffsInfoDto;
import greencity.dto.location.LocationInfoDto;
import greencity.dto.location.LocationTranslationDto;
import greencity.dto.location.LocationsDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.location.RegionTranslationDto;
import greencity.entity.order.Courier;
import greencity.entity.order.CourierLocation;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.Location;
import greencity.entity.user.Region;
import greencity.exceptions.RegionNotFoundException;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GetTariffsInfoDtoMapper extends AbstractConverter<TariffsInfo, GetTariffsInfoDto> {
    @Override
    protected GetTariffsInfoDto convert(TariffsInfo source) {
        List<CourierLocation> courierLocations = source.getCourierLocations();
        List<Location> locations = courierLocations.stream()
            .map(CourierLocation::getLocation)
            .collect(Collectors.toList());

        List<LocationsDto> locationDtos = locations.stream()
            .map(location -> LocationsDto.builder()
                .locationId(location.getId())
                .latitude(location.getCoordinates().getLatitude())
                .longitude(location.getCoordinates().getLongitude())
                .locationStatus(location.getLocationStatus().toString())
                .locationTranslationDtoList(location.getLocationTranslations().stream()
                    .map(locationTranslation -> LocationTranslationDto.builder()
                        .locationName(locationTranslation.getLocationName())
                        .languageCode(locationTranslation.getLanguage().getCode())
                        .build())
                    .collect(Collectors.toList()))
                .build())
            .collect(Collectors.toList());

        List<Region> regions = locations.stream()
            .limit(1)
            .map(Location::getRegion)
            .collect(Collectors.toList());
        List<RegionTranslationDto> regionNames = new ArrayList<>();
        for (Region r : regions) {
            List<RegionTranslationDto> regionTranslationDtos = List.of(
                    RegionTranslationDto.builder().languageCode("ua").regionName(r.getUkrName()).build(),
                    RegionTranslationDto.builder().languageCode("en").regionName(r.getEnName()).build());
                    /*-
                    r.getRegionTranslations().stream()
                .map(regionTranslation -> RegionTranslationDto.builder()
                    .regionName(regionTranslation.getName())
                    .languageCode(regionTranslation.getLanguage().getCode())
                    .build())
                .collect(Collectors.toList());
                */
            regionNames.addAll(regionTranslationDtos);

        }

        final LocationInfoDto locationInfoDto = LocationInfoDto.builder()
            .locationsDto(locationDtos)
            .regionId(
                regions.stream().findAny().orElseThrow(() -> new RegionNotFoundException("Region not found")).getId())
            .regionTranslationDtos(regionNames)
            .build();

        Set<Courier> couriers = courierLocations.stream()
            .map(CourierLocation::getCourier).collect(Collectors.toSet());

        List<CourierTranslationDto> courierTranslationDtos = new ArrayList<>();
        for (Courier c : couriers) {
            List<CourierTranslationDto> courierTranslationDtosList = c.getCourierTranslationList()
                .stream()
                .map(courierTranslation -> CourierTranslationDto.builder()
                    .name(courierTranslation.getName())
                    .languageCode(courierTranslation.getLanguage().getCode())
                    .build())
                .collect(Collectors.toList());
            courierTranslationDtos.addAll(courierTranslationDtosList);
        }

        ReceivingStationDto receivingStationDto = new ReceivingStationDto();
        receivingStationDto.setId(source.getReceivingStations().getId());
        receivingStationDto.setName(source.getReceivingStations().getName());

        GetTariffsInfoDto getTariffsInfoDto = new GetTariffsInfoDto();

        getTariffsInfoDto.setCardId(source.getId());
        getTariffsInfoDto.setLocationInfoDto(locationInfoDto);
        getTariffsInfoDto.setLocationStatus(source.getLocationStatus().getStatus());
        getTariffsInfoDto.setReceivingStationDto(receivingStationDto);
        getTariffsInfoDto.setCourierTranslationDtos(courierTranslationDtos);
        getTariffsInfoDto
            .setCreator(source.getCreator().getRecipientName() + " " + source.getCreator().getRecipientSurname());
        getTariffsInfoDto.setCreatedAt(source.getCreatedAt());
        return getTariffsInfoDto;
    }
}
