package greencity.mapping.tariff;

import greencity.dto.LocationsDtos;
import greencity.dto.RegionDto;
import greencity.dto.courier.CourierDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.employee.EmployeeNameDto;
import greencity.dto.tariff.GetTariffsInfoDto;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.Region;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class GetTariffsInfoDtoMapper extends AbstractConverter<TariffsInfo, GetTariffsInfoDto> {
    @Override
    protected GetTariffsInfoDto convert(TariffsInfo source) {
        Region region = source.getTariffLocations() != null
            ? source.getTariffLocations().iterator().next().getLocation().getRegion()
            : null;
        RegionDto regionDto = region != null ? RegionDto.builder().regionId(region.getId()).nameEn(region.getEnName())
            .nameUk(region.getUkrName()).build() : null;
        return GetTariffsInfoDto.builder()
            .cardId(source.getId())
            .courierLimit(source.getCourierLimit())
            .max(source.getMax())
            .min(source.getMin())
            .regionDto(regionDto)
            .createdAt(source.getCreatedAt())
            .creator(EmployeeNameDto.builder()
                .id(source.getCreator().getId())
                .firstName(source.getCreator().getFirstName())
                .lastName(source.getCreator().getLastName())
                .phoneNumber(source.getCreator().getPhoneNumber())
                .email(source.getCreator().getEmail()).build())
            .tariffStatus(source.getTariffStatus())
            .locationInfoDtos(source.getTariffLocations().stream()
                .map(location -> LocationsDtos.builder()
                    .locationId(location.getLocation().getId())
                    .nameEn(location.getLocation().getNameEn())
                    .nameUk(location.getLocation().getNameUk())
                    .build())
                .collect(Collectors.toList()))
            .receivingStationDtos(source.getReceivingStationList().stream()
                .map(receivingStation -> ReceivingStationDto.builder()
                    .id(receivingStation.getId())
                    .createDate(receivingStation.getCreateDate())
                    .name(receivingStation.getName())
                    .createdBy(receivingStation.getCreatedBy().getEmail())
                    .build())
                .collect(Collectors.toList()))
            .courierDto(CourierDto.builder()
                .courierId(source.getCourier().getId())
                .nameEn(source.getCourier().getNameEn())
                .nameUk(source.getCourier().getNameUk())
                .build())
            .limitDescription(source.getLimitDescription())
            .build();
    }
}
