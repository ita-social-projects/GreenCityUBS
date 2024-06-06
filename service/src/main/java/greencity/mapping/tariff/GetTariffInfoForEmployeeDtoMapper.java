package greencity.mapping.tariff;

import greencity.dto.LocationsDtos;
import greencity.dto.RegionDto;
import greencity.dto.courier.CourierTranslationDto;
import greencity.dto.courier.GetReceivingStationDto;
import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
import greencity.entity.order.TariffsInfo;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetTariffInfoForEmployeeDtoMapper extends AbstractConverter<TariffsInfo, GetTariffInfoForEmployeeDto> {
    @Override
    protected GetTariffInfoForEmployeeDto convert(TariffsInfo source) {
        RegionDto regionDto = source.getTariffLocations().stream().map(
            tariffLocation -> RegionDto.builder()
                .regionId(tariffLocation.getLocation().getRegion().getId())
                .nameUk(tariffLocation.getLocation().getRegion().getUkrName())
                .nameEn(tariffLocation.getLocation().getRegion().getEnName())
                .build())
            .findFirst().orElse(null);

        List<LocationsDtos> locationsDtos = source.getTariffLocations().stream()
            .map(tariffLocation -> LocationsDtos.builder()
                .locationId(tariffLocation.getLocation().getId())
                .nameUk(tariffLocation.getLocation().getNameUk())
                .nameEn(tariffLocation.getLocation().getNameEn())
                .build())
            .toList();

        List<GetReceivingStationDto> getReceivingStationDtos = source.getReceivingStationList()
            .stream()
            .map(receivingStation -> GetReceivingStationDto.builder()
                .stationId(receivingStation.getId())
                .name(receivingStation.getName())
                .build())
            .toList();

        return GetTariffInfoForEmployeeDto.builder()
            .id(source.getId())
            .region(regionDto)
            .locationsDtos(locationsDtos)
            .receivingStationDtos(getReceivingStationDtos)
            .courier(CourierTranslationDto.builder()
                .id(source.getCourier().getId())
                .nameEn(source.getCourier().getNameEn())
                .nameUk(source.getCourier().getNameUk()).build())
            .build();
    }
}
