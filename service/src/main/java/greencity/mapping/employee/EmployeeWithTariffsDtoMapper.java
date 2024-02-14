package greencity.mapping.employee;

import greencity.dto.LocationsDtos;
import greencity.dto.RegionDto;
import greencity.dto.courier.CourierTranslationDto;
import greencity.dto.courier.GetReceivingStationDto;
import greencity.dto.employee.EmployeeWithTariffsDto;
import greencity.dto.employee.EmployeeDto;
import greencity.dto.position.PositionDto;
import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
import greencity.entity.user.employee.Employee;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class EmployeeWithTariffsDtoMapper extends AbstractConverter<Employee, EmployeeWithTariffsDto> {
    @Override
    protected EmployeeWithTariffsDto convert(Employee employee) {
        return EmployeeWithTariffsDto.builder()
            .employeeDto(EmployeeDto.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .image(employee.getImagePath())
                .employeePositions(employee.getEmployeePosition().stream()
                    .map(position -> PositionDto.builder()
                        .id(position.getId())
                        .name(position.getName())
                        .nameEn(position.getNameEn())
                        .build())
                    .collect(Collectors.toList()))
                .build())
            .tariffs(employee.getTariffInfos().stream()
                .map(getTariffs -> GetTariffInfoForEmployeeDto.builder()
                    .id(getTariffs.getId())
                    .region(getTariffs.getTariffLocations().stream().map(
                        tariffLocation -> RegionDto.builder()
                            .regionId(tariffLocation.getLocation().getRegion().getId())
                            .nameUk(tariffLocation.getLocation().getRegion().getUkrName())
                            .nameEn(tariffLocation.getLocation().getRegion().getEnName())
                            .build())
                        .toList().getFirst())
                    .locationsDtos(getTariffs.getTariffLocations().stream()
                        .map(tariffLocation -> LocationsDtos.builder()
                            .locationId(tariffLocation.getLocation().getId())
                            .nameUk(tariffLocation.getLocation().getNameUk())
                            .nameEn(tariffLocation.getLocation().getNameEn())
                            .build())
                        .collect(Collectors.toList()))
                    .receivingStationDtos(getTariffs.getReceivingStationList().stream()
                        .map(receivingStation -> GetReceivingStationDto.builder()
                            .stationId(receivingStation.getId())
                            .name(receivingStation.getName())
                            .build())
                        .collect(Collectors.toList()))
                    .courier(CourierTranslationDto.builder()
                        .id(getTariffs.getCourier().getId())
                        .nameEn(getTariffs.getCourier().getNameEn())
                        .nameUk(getTariffs.getCourier().getNameUk()).build())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}