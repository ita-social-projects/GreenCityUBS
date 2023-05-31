package greencity.mapping.employee;

import greencity.dto.LocationsDtos;
import greencity.dto.RegionDto;
import greencity.dto.courier.CourierTranslationDto;
import greencity.dto.courier.GetReceivingStationDto;
import greencity.dto.employee.GetEmployeeDto;
import greencity.dto.position.PositionDto;
import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
import greencity.entity.user.employee.Employee;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetEmployeeDtoMapper extends AbstractConverter<Employee, GetEmployeeDto> {
    @Override
    protected GetEmployeeDto convert(Employee source) {
        List<PositionDto> positionDtos = source.getEmployeePosition()
            .stream()
            .map(position -> PositionDto.builder()
                .id(position.getId())
                .name(position.getName())
                .build())
            .collect(Collectors.toList());

        List<GetTariffInfoForEmployeeDto> tariffInfoForEmployeeDtos = source.getTariffInfos()
            .stream().map(tariffsInfo -> GetTariffInfoForEmployeeDto.builder()
                .id(tariffsInfo.getId())
                .courier(CourierTranslationDto.builder()
                    .id(tariffsInfo.getCourier().getId())
                    .nameUk(tariffsInfo.getCourier().getNameUk())
                    .nameEn(tariffsInfo.getCourier().getNameEn()).build())
                .locationsDtos(tariffsInfo.getTariffLocations()
                    .stream()
                    .map(tariffLocation -> LocationsDtos.builder()
                        .locationId(tariffLocation.getLocation().getId())
                        .nameUk(tariffLocation.getLocation().getNameUk())
                        .nameEn(tariffLocation.getLocation().getNameEn())
                        .build())
                    .collect(Collectors.toList()))
                .receivingStationDtos(tariffsInfo.getReceivingStationList()
                    .stream()
                    .map(receivingStation -> GetReceivingStationDto.builder()
                        .stationId(receivingStation.getId())
                        .name(receivingStation.getName())
                        .build())
                    .collect(Collectors.toList()))
                .region(tariffsInfo.getTariffLocations().stream()
                    .findFirst()
                    .map(tariffLocation -> RegionDto.builder()
                        .regionId(tariffLocation.getLocation().getRegion().getId())
                        .nameEn(tariffLocation.getLocation().getRegion().getEnName())
                        .nameUk(tariffLocation.getLocation().getRegion().getUkrName())
                        .build())
                    .orElse(null))
                .build())
            .collect(Collectors.toList());
        return GetEmployeeDto.builder()
            .id(source.getId())
            .firstName(source.getFirstName())
            .lastName(source.getLastName())
            .phoneNumber(source.getPhoneNumber())
            .email(source.getEmail())
            .image(source.getImagePath())
            .employeePositions(positionDtos)
            .tariffs(tariffInfoForEmployeeDtos)
            .build();
    }
}
